/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

metadata {
	definition (name: "Robot Shield JGLS", namespace: "smartthings", author: "SmartThings") {
    	capability "Actuator"
		capability "Switch"
		capability "Sensor"
        
        attribute "pushB1", "enum", ["B1On", "B1Off"]
        attribute "pushB2", "enum", ["B2On", "B2Off"]
        attribute "optoelectronic", "string"
        attribute "potenciometer", "number"
        attribute "cmdReceived",  "enum", ["cmdOk", "noCmd"]
        //attribute "motorstatus", "enum", ["motorstOn", "motorstOff"]
        
        command "commandRobot", ["string"]
        command "setOn"
        command "setOff"
        command "confirmCommand", ["string"]
        
        command "setRangedLevel", ["number"]
	}
	// Simulator metadata
    simulator {
    	status "on":  "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6E"
		status "off": "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6666"
        
		// reply messages
		reply "raw 0x0 { 00 00 0a 0a 6f 6e }": "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6E"
		reply "raw 0x0 { 00 00 0a 0a 6f 66 66 }": "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6666"
	}
	
	tiles(scale: 2) {
    	standardTile("switch", "device.switch", width: 3, height: 3, canChangeIcon: true, canChangeBackground: true, decoration: "ring") {
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}
        standardTile("pushButton1", "device.pushB1", width: 3, height: 3, decoration: "ring") {
        	state "B1Off", label: "Button 1 Off", icon: "st.Appliances.appliances1", backgroundColor: "#ffffff"
			state "B1On", label: "Button 1 On", icon: "st.Appliances.appliances1", backgroundColor: "#79b821"
        }
        standardTile("pushB2", "device.pushB2", width: 3, height: 3, decoration: "ring") {
        	state "B2Off", label: "Button 2 Off", icon: "st.Appliances.appliances11", backgroundColor: "#ffffff"
			state "B2On", label: "Button 2 On", icon: "st.Appliances.appliances11", backgroundColor: "#79b821"
        }
        standardTile("optoelect", "device.optoelectronic", width: 3, height: 3, decoration: "ring") {
			state "sensorOff", label: '${name}', icon: "st.Appliances.appliances1", backgroundColor: "#ffffff", defaultState: true
            state "sensorOn", label: '${name}', icon: "st.Appliances.appliances1", backgroundColor: "#79b821"
        }
        standardTile("motor", "device.pinstatus", width: 3, height: 3, decoration: "ring") {
        	state "stOff", label: "Led Off", action: "setOn", icon: "st.Appliances.appliances11", backgroundColor: "#ffffff", nextState:"stOn"
			state "stOn", label: "Led On", action: "setOff", icon: "st.Appliances.appliances11", backgroundColor: "#79b821", nextState:"stOff"
		}
        controlTile("rangeSlider", "device.rangedLevel", "slider", height: 2, width: 6, inactiveLabel: false, range: "(0..255)") {
			state "level", action:"setRangedLevel"
		}
		valueTile("rangeValue", "device.rangedLevel", height: 2, width: 2) {
			state "range", label:'${currentValue}', backgroundColor: "#e86d13", defaultState: true
		}
        
        valueTile("potenciometer", "device.potenciometer", width: 3, height: 3) {
            state("val", label:'${currentValue}', unit:"dF", defaultState: true,
                backgroundColors:[
                    [value: 34, color: "#153591"],
                    [value: 199, color: "#1e9cbb"],
                    [value: 364, color: "#90d2a7"],
                    [value: 529, color: "#44b621"],
                    [value: 694, color: "#f1d801"],
                    [value: 859, color: "#d04e00"],
                    [value: 1010, color: "#bc2323"]
                ]
            )
        }
        
		main (["switch"])
        details(["switch","pushButton1","pushB2",
        		 "optoelect","potenciometer",
                 "motor","rangeSlider"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	//log.debug "Description: $description"
	def value = zigbee.parse(description)?.text
    def name = null
    def subvalue = null
    //def name = value in ["on","off"] ? "switch" : null
    	
    switch (value) {
        case "B1On":
        	//log.debug "Push B1: ENCENDIDO"
        	name = "pushB1"
        	break
        case "B1Off":
        	//log.debug "Push B1: APAGADO"
        	name = "pushB1"
        	break
        case "B2On":
        	//log.debug "Push B2: ENCENDIDO"
        	name = "pushB2"
        	break
        case "B2Off":
        	//log.debug "Push B2: APAGADO"
        	name = "pushB2"
        	break
        case "on":
        	//log.debug "Switch: ENCENDIDO"
        	name = "switch"
        	break
        case "off":
        	//log.debug "Swtich: APAGADO"
        	name = "switch"
        	break
        case "sensorOn":
        	//log.debug "Optoelectronic: ENCENDIDO"
        	name = "optoelectronic"
        	break
        case "sensorOff":
        	//log.debug "Optoelectronic: APAGADO"
        	name = "optoelectronic"
        	break
        case "stOn":
        	//log.debug "Motor: ENCENDIDO"
        	name = "pinstatus"
        	break
        case "stOff":
        	//log.debug "Motor: APAGADO"
        	name = "pinstatus"
        	break
        case "cmdOk":
        	log.debug "Comando Robot OK"
        	name = "cmdReceived"
        	break
        default:
            name = null
    }
    
    
    if(!name)
    {
    	if (value?.length() > 3)
        {
        	subvalue = value.take(2)
            log.debug "Comprueba Range Level"
            if(subvalue == "sL")
            {
                def valRange = value.split('-')
                value = Integer.parseInt(valRange[1])
                log.debug "Valor Range: ${valRange[1]}"
                name = "rangedLevel"
            }
            else if (value.length() > 5)
            {
                subvalue = value.take(4)
                log.debug "Comprueba Pot"
                if(subvalue == "pot1")
                {
                    def valPot = value.split('-')
                    //value = valPot[1].toInteger()
                    value = Integer.parseInt(valPot[1])
                    log.debug "Valor Pot1: ${valPot[1]}"
                    name = "potenciometer"
                    //log.debug valOptoNumeric
                }
            }
        }
    }
    //log.debug "Evento: ${name}: ${value}"
    
    def result = createEvent(name: name, value: value, isStateChange: true, displayed: true)
	log.debug "Parse returned ${result?.descriptionText}"
	return result
}

// Commands sent to the device
def on() {
	zigbee.smartShield(text: "on").format()
}

def off() {
	zigbee.smartShield(text: "off").format()
}

// this command takes parameters as defined in the definition
def commandRobot(def stringParam) {
    // handle command
    log.debug "Command send to ROS: ${"cmd" + stringParam}"
    zigbee.smartShield(text: "cmd-" + stringParam).format()
}

def confirmCommand(def stringConfirm){
	if(stringConfirm == "ok")
    {
    	log.debug "Comando recibido por el robot"
    	sendEvent(name: "cmdReceived", value: "noCmd")
    }
}

def setOn() {
    // handle command
    log.debug "Command: Set On"
    zigbee.smartShield(text: "pinOn").format()
}

def setOff() {
    // handle command
    log.debug "Command: Set On"
    zigbee.smartShield(text: "pinOff").format()
}

def setRangedLevel(value) {
    log.debug "setting ranged level to $value"
    zigbee.smartShield(text: "sL-$value").format()
	//sendEvent(name: "rangedLevel", value: value)
}

/*
def installed() {
	log.debug "It has been installed"
    sendEvent(name: "pushB1", value: "pushB1Off")
    sendEvent(name: "pushB2", value: "pushB2Off")
    sendEvent(name: "optoelectronic", value: "sensorOff")
    sendEvent(name: "potenciometer", value: 74)
    sendEvent(name: "rangedLevel", value: 47)
}*/