/**
 *  Test
 *
 *  Copyright 2016 Pete Lewis
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
definition(
    name: "Test",
    namespace: "PJayB",
    author: "Pete Lewis",
    description: "Learning How To SmartThings",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("When all of these people leave home") {
        input "people", "capability.presenceSensor", multiple: true, title: "Which people?"
    }
    section("Check this sensor") {
		input "thesensor", "capability.contactSensor", required: true, title: "What?" 
	}
    section("Send Push Notification?") {
        input "sendPush", "bool", required: false,
              title: "Send Push Notification if open?"
    }
    section("Send a text message to this number (optional)") {
        input "phone", "phone", required: false
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
    log.debug "Installed with settings: ${settings}"
    log.debug "Current mode = ${location.mode}, people = ${people.collect{it.label + ': ' + it.currentPresence}}"
    subscribe(people, "presence", presence)
//	subscribe(thesensor, "contact.open", contactHandler)
}

def presence(evt) {
	if (!thesensor) {
    	log.debug "No sensor specified: ignoring"
        return
    }

    log.debug "evt.name: $evt.value"
    if (evt.value == "not present") {
        if (location.mode != newMode) {
            log.debug "checking if everyone is away"
            if (everyoneIsAway()) {
                log.debug "starting sequence"
                
                def contactState = thesensor.contactState?.value
            	log.debug "${thesensor.displayName} is ${contactState}"    
			                
                if (contactState == "open") {
                	def message = "${thesensor.displayName} is ${contactState}"
                  	if (sendPush) {
                    	log.debug "Sending Push Notification"
                  		sendPush(message)
                    } else {
                    	log.debug "Push notifications are disabled"
                    }
                    if (phone) {
                    	log.debug "Sending SMS"
                    	sendSms(message)
                    } else {
                    	log.debug "SMS notifications are disabled"
                    }
                }
            }
        }
        else {
            log.debug "mode is the same, not evaluating"
        }
    }
    else {
        log.debug "present; doing nothing"
    }
}

// returns true if all configured sensors are not present,
// false otherwise.
private everyoneIsAway() {
    def result = true
    // iterate over our people variable that we defined
    // in the preferences method
    for (person in people) {
        if (person.currentPresence == "present") {
            // someone is present, so set our our result
            // variable to false and terminate the loop.
            result = false
            break
        }
    }
    log.debug "everyoneIsAway: $result"
    return result
}

//def contactHandler(evt) {
//    log.debug "Contact is in ${evt.value} state"
//    
//    def message = "${thesensor.displayName} is ${thesensor.contactState.value}"
//    if (sendPush) {
//        sendPush(message)
//    }
//    if (phone) {
//        sendSms(phone, message)
//    }
//}
