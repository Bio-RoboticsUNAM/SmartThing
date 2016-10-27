/**
 *  Copyright 2015 Bio-Robótica UNAM
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
	definition (name: "Magnetic Contact Sensor", namespace: "biorobotica.unam", author: "Bio-Robótica UNAM") {
		capability "Contact Sensor"
	}
    
    tiles {
		standardTile("contact", "device.contact", width: 2, height: 2) {
			state "closed", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#79b821"
			state "open", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#ffa81e"
		}

		main "contact"
		details "contact"
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	def value = zigbee.parse(description)?.text
	def name = value in ["open","closed"] ? "contact" : null
	def result = createEvent(name: name, value: value)
	return result
}

// Commands sent to the device
/*def on() {
	zigbee.smartShield(text: "on").format()
}

def off() {
	zigbee.smartShield(text: "off").format()
}*/
