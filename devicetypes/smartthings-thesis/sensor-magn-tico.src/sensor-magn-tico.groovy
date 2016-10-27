/* Configuración del dispositivo */
metadata {
	definition (name: "Sensor magnético", namespace: "smartthings-thesis", author: "UNAM") {
		capability "Actuator"
		capability "Switch"
		capability "Sensor"
	}

	// Para simulación
	simulator {
		status "on":  "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6E"
		status "off": "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6666"

		// Mensajes de respuesta
		reply "raw 0x0 { 00 00 0a 0a 6f 6e }": "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6E"
		reply "raw 0x0 { 00 00 0a 0a 6f 66 66 }": "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6666"
	}

	// Definición del mosaico de la interfaz de usuario
	tiles {
		standardTile("switchTile", "device.switch", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {
			state "on", label: '${name}', action: "switch.on", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "off", label: '${name}', action: "switch.off", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}
		standardTile("messageTile", "device.switch", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {
			state "on", label: '${name}', action: "switch.on", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "off", label: '${name}', action: "switch.off", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}

		main "switch"
		details "switch"
	}
}

// Traducción de mensajes recibidos, para generar eventos en la nube
def parse(String description) {
	def value = zigbee.parse(description)?.text
	def name = value in ["on","off"] ? "switch" : null
	def result = createEvent(name: name, value: value)
	log.debug "Parse returned ${result?.descriptionText}"
	return result
}

// Commandos enviados al shield
def on() {
	// Acción cuando el sensor se activa
}

def off() {
	// Acción cuando el sensor se desactiva
}

def message() {
	zigbee.smartShield(text: "message").format()
}