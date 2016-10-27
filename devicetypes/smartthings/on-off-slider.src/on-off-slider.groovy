/*
* Esta aplicacion hace lo siguiente
* -Procesa la entrada de un SLIDER o Barra en la app SMARTTHINGS
* controla una señal PWM a travez del valor enviado de SMARTTHINGS
* Envia un mensaje de la nube (SmartThing App), 
* procesa con una salidad en un LED, TRIAC o MOTOR
*  recibe un mensaje para indicar el estado de PWM
 * Author: Rocha Resendiz Irving
 * Dr. Savage Carmona Jesus
 * UNAM- Bio-Robotics
 * Date: 2016-06-16
 */

metadata {
	definition (name: "On/Off Slider", namespace: "smartthings", author: "biorobotics") {
		capability "Actuator"
		capability "Switch"
		capability "Sensor"
        
        capability "Switch Level"
		capability "Color Control"
		capability "Configuration"
		capability "Polling"
		capability "Refresh"

		command "setAdjustedColor"
		command "setLevel"
	}

	// Simulator metadata
	simulator {
		status "on":  "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6E"
		status "off": "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6666"

		// reply messages
		reply "raw 0x0 { 00 00 0a 0a 6f 6e }": "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6E"
		reply "raw 0x0 { 00 00 0a 0a 6f 66 66 }": "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6666"
	}

	// UI tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}
        
        controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false) {
			state "level", action:"switch level.setLevel"
		}
		valueTile("level", "device.level", inactiveLabel: false, decoration: "flat") {
			state "level", label: 'Level ${currentValue}%'
		}
		controlTile("saturationSliderControl", "device.saturation", "slider", height: 1, width: 2, inactiveLabel: false) {
			state "saturation", action:"color control.setSaturation"
		}
		valueTile("saturation", "device.saturation", inactiveLabel: false, decoration: "flat") {
			state "saturation", label: 'Sat ${currentValue}    '
		}
		controlTile("hueSliderControl", "device.hue", "slider", height: 1, width: 2, inactiveLabel: false) {
			state "hue", action:"color control.setHue"
		}

		main(["switch"])
		details(["switch", "levelSliderControl", "refresh"])
	}
}


Map parse(String description) {

	def value = zigbee.parse(description)?.text
	def linkText = getLinkText(device)
	def descriptionText = getDescriptionText(description, linkText, value)
	def handlerName = value
	def isStateChange = value != "ping"
	def displayed = value && isStateChange

	def result = [
		value: value,
		name: value in ["on","off"] ? "switch" : (value && value != "ping" ? "greeting" : null),
		handlerName: handlerName,
		linkText: linkText,
		descriptionText: descriptionText,
		isStateChange: isStateChange,
		displayed: displayed
	]

	log.debug result.descriptionText
	result
}

def setHue(value) {
	def max = 0xfe
	log.trace "setHue($value)"
    zigbee.smartShield(text: "$value").format()
	sendEvent(name: "hue", value: value)
	def scaledValue = Math.round(value * max / 100.0)
	//def cmd = "st cmd 0x${device.deviceNetworkId} ${endpointId} 0x300 0x00 {${hex(scaledValue)} 00 0000}"
    zigbee.smartShield(text: "$scaledValue").format()
    log.trace "$scaledValue"
	//log.info cmd
	cmd
}

def setAdjustedColor(value) {
	log.debug "setAdjustedColor: ${value}"
	def adjusted = value + [:]
	adjusted.hue = adjustOutgoingHue(value.hue)
	adjusted.level = null // needed because color picker always sends 100
	setColor(adjusted)
}

def setColor(value){
	log.trace "setColor($value)"
    zigbee.smartShield(text: "$value").format()
	def max = 0xfe

	sendEvent(name: "hue", value: value.hue)
	sendEvent(name: "saturation", value: value.saturation)
	def scaledHueValue = Math.round(value.hue * max / 100.0)
	def scaledSatValue = Math.round(value.saturation * max / 100.0)

	def cmd = []
	if (value.switch != "off" && device.latestValue("switch") == "off") {
		cmd << "st cmd 0x${device.deviceNetworkId} ${endpointId} 6 1 {}"
		cmd << "delay 150"
	}

	cmd << "st cmd 0x${device.deviceNetworkId} ${endpointId} 0x300 0x00 {${hex(scaledHueValue)} 00 0000}"
	cmd << "delay 150"
	cmd << "st cmd 0x${device.deviceNetworkId} ${endpointId} 0x300 0x03 {${hex(scaledSatValue)} 0000}"

	if (value.level != null) {
		cmd << "delay 150"
		cmd.addAll(setLevel(value.level))
	}

	if (value.switch == "off") {
		cmd << "delay 150"
		cmd << off()
	}
	log.info cmd
	cmd
}

def setSaturation(value) {
	def max = 0xfe
	log.trace "setSaturation($value)"
    zigbee.smartShield(text: "$value").format()
	sendEvent(name: "saturation", value: value)
	def scaledValue = Math.round(value * max / 100.0)
	def cmd = "st cmd 0x${device.deviceNetworkId} ${endpointId} 0x300 0x03 {${hex(scaledValue)} 0000}"
	//log.info cmd
	cmd
}

def refresh() {
	"st rattr 0x${device.deviceNetworkId} 1 6 0"
}

def poll(){
	log.debug "Poll is calling refresh"
	refresh()
}

def setLevel(value) {
	log.trace "setLevel($value)"
    zigbee.smartShield(text: "setLevel($value)").format()
	def cmds = []

	if (value == 0) {
		sendEvent(name: "switch", value: "off")
		cmds << "st cmd 0x${device.deviceNetworkId} ${endpointId} 6 0 {}"
	}
	else if (device.latestValue("switch") == "off") {
		sendEvent(name: "switch", value: "on")
	}

	sendEvent(name: "level", value: value)
    def level = hexString(Math.round(value * 255))
	cmds << "st cmd 0x${device.deviceNetworkId} ${endpointId} 8 4 {${level} 0000}"

	//log.debug cmds
	cmds
    on(value)
}

private getEndpointId() {
	new BigInteger(device.endpointId, 16).toString()
}

private hex(value, width=2) {
	def s = new BigInteger(Math.round(value).toString()).toString(16)
	while (s.size() < width) {
		s = "0" + s
	}
	s
}

private adjustOutgoingHue(percent) {
	def adjusted = percent
	if (percent > 31) {
		if (percent < 63.0) {
			adjusted = percent + (7 * (percent -30 ) / 32)
		}
		else if (percent < 73.0) {
			adjusted = 69 + (5 * (percent - 62) / 10)
		}
		else {
			adjusted = percent + (2 * (100 - percent) / 28)
		}
	}
	log.info "percent: $percent, adjusted: $adjusted"
	adjusted
}

// Commands sent to the device
def on(value) {
	log.trace "setLevelOn($value)" 
	zigbee.smartShield(text: value).format()
}

def off() {
	zigbee.smartShield(text: "off").format()
}