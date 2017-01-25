/**
 *  Particle Photon Irrigation Controller 8 Zones
 *  This SmartThings Device Type Code Works With Particle Photon Irrigation Controller
 *  This code is a modified version of Irrigation Controller 8 Zones v2.67 to work with
 *  the Particle Photon and to recursively create zone tiles and commands
 *
 *
 *	Smarthings Device/APP Credit to the following:
 *  Author: Stan Dotson (stan@dotson.info) and Matthew Nichols (matt@nichols.name)
 *  Date: 2014-06-14
 *  Copyright 2014 Stan Dotson and Matthew Nichols
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */

//Set the actual number of zones in the "def"
int nZones = nZones()
def zStatePrev = []

 preferences {
    input("deviceId", "text", title: "Device ID")
    input("token", "text", title: "Access Token")
	
    
     for (int i=1;i<nZones+1;i++)
     {
     	input("Timer_${i}", "text", title: "Zone ${i}", description: "Zone ${i} Time", required: false)
     }
}

 // for the UI
metadata {
    // Automatically generated. Make future change here.
	definition (name: "Particle - Irrigation Controller 8 Zones v0.1", version: "0.1", author: "anthony cordon") {
        capability "Momentary"; capability "Switch"
        capability "Sensor"; capability "signalStrength"
        capability "Polling"; capability "Refresh"
       	command "OnWithZoneTimes"
        command "rainDelayed"
        command "update" 
        command "enablePump"
        command "disablePump"
        command "onPump"
        command "offPump"
        command "noEffect"
        command "skip"
        command "expedite"
        command "onHold"
        attribute "effect", "string"      
		//command "RelayOn"; command "RelayOnFor"; command "RelayOff"
        
        for (int i=1;i<nZones+1;i++)
        {
			command "RelayOn${i}"; command "RelayOnFor${i}"; command "RelayOff${i}"
        }

	}

    // tile definitions
	tiles {
		//Refresh and Status Tiles
		valueTile("signalStrength", "device.signalStrength", width: 1, height: 1)
        {
            state "default", label: '${currentValue}', unit:"",backgroundColor: "#79b821"
        }
		
        
        standardTile("refreshTile", "device.refresh", inactiveLabel: false, decoration: "flat") 
        {
            state "default", action:"polling.poll", icon:"st.secondary.refresh"
        }

        standardTile("scheduleEffect", "device.effect", width: 1, height: 1) {
            state("noEffect", label: "Normal", action: "skip", icon: "st.Office.office7", backgroundColor: "#ffffff")
            state("skip", label: "Skip 1X", action: "expedite", icon: "st.Office.office7", backgroundColor: "#c0a353")
            state("expedite", label: "Expedite", action: "onHold", icon: "st.Office.office7", backgroundColor: "#53a7c0")
            state("onHold", label: "Pause", action: "noEffect", icon: "st.Office.office7", backgroundColor: "#bc2323")
        }

		//Smart Irrigation Tiles
		standardTile("allZonesTile", "device.switch", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
            state "off", label: 'Start', action: "switch.on", icon: "st.Outdoor.outdoor12", backgroundColor: "#ffffff", nextState: "starting"
            state "on", label: 'Running', action: "switch.off", icon: "st.Health & Wellness.health7", backgroundColor: "#53a7c0", nextState: "stopping"
            state "starting", label: 'Starting...', action: "switch.off", icon: "st.Health & Wellness.health7", backgroundColor: "#53a7c0"
            state "stopping", label: 'Stopping...', action: "switch.off", icon: "st.Health & Wellness.health7", backgroundColor: "#53a7c0"
            state "rainDelayed", label: 'Rain Delay', action: "switch.off", icon: "st.Weather.weather10", backgroundColor: "#fff000", nextState: "off"
            state "autoStarting", label: 'Program starting...', action: "switch.off", icon: "st.Health & Wellness.health7", backgroundColor: "#53a7c0"        
        }

		
        def zoneNames = ["Front\nLawn","Front\nDrip","Rear\nLawn","Rear\nDrip1","Rear\nDrip2","Rear\nDrip3","Front\nVerge","None"]
        
        for (int i=1;i<nZones+1;i++)
            standardTile("zone_${i}_Tile", "device.zone_${i}", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true)
            {
                state "off${i}", label: "${zoneNames[i-1]}", action: "RelayOn${i}", icon: "st.Outdoor.outdoor12", backgroundColor: "#ffffff",nextState: "sending${i}"
                state "sending${i}", label: "sending", action: "RelayOff${i}", icon: "st.Health & Wellness.health7", backgroundColor: "#cccccc"
                state "q${i}", label: "${zoneNames[i-1]}", action: "RelayOff${i}",icon: "st.Outdoor.outdoor12", backgroundColor: "#c0a353", nextState: "sending${i}"
                state "on${i}", label: "${zoneNames[i-1]}", action: "RelayOff${i}",icon: "st.Outdoor.outdoor12", backgroundColor: "#53a7c0", nextState: "sending${i}"
                state "sendingOff${i}", label: "sending", action: "RelayOff${i}", icon: "st.Health & Wellness.health7", backgroundColor: "#cccccc"
            }

        standardTile("pumpTile", "device.pump", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
            state "noPump", label: 'Pump', action: "enablePump", icon: "st.custom.buttons.subtract-icon", backgroundColor: "#ffffff",nextState: "enablingPump"
         	state "offPump", label: 'Pump', action: "onPump", icon: "st.valves.water.closed", backgroundColor: "#ffffff", nextState: "sendingPump"
           	state "enablingPump", label: 'sending', action: "disablePump", icon: "st.Health & Wellness.health7", backgroundColor: "#cccccc"
            state "disablingPump", label: 'sending', action: "disablePump", icon: "st.Health & Wellness.health7", backgroundColor: "#cccccc"
            state "onPump", label: 'Pump', action: "offPump",icon: "st.valves.water.open", backgroundColor: "#53a7c0", nextState: "sendingPump"
            state "sendingPump", label: 'sending', action: "offPump", icon: "st.Health & Wellness.health7", backgroundColor: "#cccccc"
        }
        
		main "allZonesTile"
        def detailList = ["allZonesTile"]
        
        for (int i=1;i<nZones+1;i++)
        	detailList.push("zone_${i}_Tile")
            
        detailList.addAll(["pumpTile","scheduleEffect","refreshTile","signalStrength"])
        
        details(detailList)
		
	}
}


//Set the number of zones here
def nZones() {
	return 8
}

//Sample LED commands
def parse(String description) {
	log.error "This device does not support incoming events"
	return null
}

def poll() {
	log.debug "Executing 'poll'"
    getResp("v_state")
    getVar("signalStrength","wifiStrength")
}

def refresh() {
	log.debug "Executing 'refresh'"
    getResp("v_state")
    //getVar("signalStrength","wifiStrength")
}


// parse events into attributes to create events
def str_parse(String description) {
//    log.debug "Parsing '${description}'"
//    log.debug "Parsed: ${zigbee.parse(description)}"
  
    def value = description
    log.debug "Parsed: ${value}"
    if (value != null && value != " " && value != '"' && value != "ping" && value != "havePump" && value != "noPump") {
        String delims = ","
        String[] tokens = value.split(delims)
        for (int x=0; x<tokens.length; x++) {
            def displayed = tokens[x]  //evaluates whether to display message

            def name = tokens[x] in ["on1", "q1", "off1"] ? "zone_1"
            : tokens[x] in ["on2", "q2", "off2"] ? "zone_2"
            : tokens[x] in ["on3", "q3", "off3"] ? "zone_3"
            : tokens[x] in ["on4", "q4", "off4"] ? "zone_4"
            : tokens[x] in ["on5", "q5", "off5"] ? "zone_5"
            : tokens[x] in ["on6", "q6", "off6"] ? "zone_6"
            : tokens[x] in ["on7", "q7", "off7"] ? "zone_7"
            : tokens[x] in ["on8", "q8", "off8"] ? "zone_8"
            : tokens[x] in ["onPump", "offPump"] ? "pump"
            : tokens[x] in ["ok"] ? "refresh" : null

            def currentVal = device.currentValue(name)

            def stateChange = true
            // It seems like this should work. When a state change is made due to a nextState parameter, the value is not changed.
            // if(currentVal) stateChange = currentVal != tokens[x]

            def result = createEvent(name: name, value: tokens[x], displayed: true, isStateChange: true, isPhysical: true)
            log.debug "Parse returned ${result?.descriptionText}"
            sendEvent(result)
        }
    }
    if (value == "pumpAdded") {
    	sendEvent (name:"zone_8", value:"havePump", displayed: true, isStateChange: true, isPhysical: true)
        sendEvent (name:"pump", value:"offPump", displayed: true, isStateChange: true, isPhysical: true)
    }
    if (value == "pumpRemoved") {
    	sendEvent (name:"pump", value:"noPump", displayed: true, isStateChange: true, isPhysical: true)
    }

	
    if(anyZoneOn()) {
        sendEvent(name: "switch", value: "on", displayed: true)
    } else if (device.currentValue("switch") != "rainDelayed") {
        sendEvent(name: "switch", value: "off", displayed: true)
    }
    
}

//Check if any zones are on
int anyZoneOn() {
	int fl = 0;
	for (int i=1;i<nZones()+1;i++)
    {
    	if(device.currentValue("zone_${i}").contains('on')) fl=1;
        if(device.currentValue("zone_${i}").contains('q')) fl=1;
    }
     return fl
}


// handle commands for relays

def RelayOn1()
{
    cmdRelay "<v_On>1</v_On>"
    refresh()
}
def RelayOn1For(value) 
{
    value = checkTime(value)
    cmdRelay "<v_OnFor>1,$value</v_OnFor>"
    refresh()
}
def RelayOff1() 
{
    cmdRelay "<v_Off>1</v_Off>"
    refresh()
}
def RelayOn2()
{
    cmdRelay "<v_On>2</v_On>"
    refresh()
}
def RelayOn2For(value) 
{
    value = checkTime(value)
    cmdRelay "<v_OnFor>2,$value</v_OnFor>"
    refresh()
}
def RelayOff2() 
{
    cmdRelay "<v_Off>2</v_Off>"
    refresh()
}
def RelayOn3()
{
    cmdRelay "<v_On>3</v_On>"
    refresh()
}
def RelayOn3For(value) 
{
    value = checkTime(value)
    cmdRelay "<v_OnFor>3,$value</v_OnFor>"
    refresh()
}
def RelayOff3() 
{
    cmdRelay "<v_Off>3</v_Off>"
    refresh()
}
def RelayOn4()
{
    cmdRelay "<v_On>4</v_On>"
    refresh()
}
def RelayOn4For(value) 
{
    value = checkTime(value)
    cmdRelay "<v_OnFor>4,$value</v_OnFor>"
    refresh()
}
def RelayOff4() 
{
    cmdRelay "<v_Off>4</v_Off>"
    refresh()
}
def RelayOn5()
{
    cmdRelay "<v_On>5</v_On>"
    refresh()
}
def RelayOn5For(value) 
{
    value = checkTime(value)
    cmdRelay "<v_OnFor>5,$value</v_OnFor>"
    refresh()
}
def RelayOff5() 
{
    cmdRelay "<v_Off>5</v_Off>"
    refresh()
}
def RelayOn6()
{
    cmdRelay "<v_On>6</v_On>"
    refresh()
}
def RelayOn6For(value) 
{
    value = checkTime(value)
    cmdRelay "<v_OnFor>6,$value</v_OnFor>"
    refresh()
}
def RelayOff6() 
{
    cmdRelay "<v_Off>6</v_Off>"
    refresh()
}
def RelayOn7()
{
    cmdRelay "<v_On>7</v_On>"
    refresh()
}
def RelayOn7For(value) 
{
    value = checkTime(value)
    cmdRelay "<v_OnFor>7,$value</v_OnFor>"
    refresh()
}
def RelayOff7() 
{
    cmdRelay "<v_Off>7</v_Off>"
    refresh()
}
def RelayOn8()
{
    cmdRelay "<v_On>8</v_On>"
    refresh()
}
def RelayOn8For(value) 
{
    value = checkTime(value)
    cmdRelay "<v_OnFor>8,$value</v_OnFor>"
    refresh()
}
def RelayOff8() 
{
    cmdRelay "<v_Off>8</v_Off>"
    refresh()
}

def on() 
{
    cmdRelay "<v_OnFor>1,${Timer_1 ?: 0}</v_OnFor>"
    cmdRelay "<v_OnFor>2,${Timer_2 ?: 0}</v_OnFor>"
    cmdRelay "<v_OnFor>3,${Timer_3 ?: 0}</v_OnFor>"
    cmdRelay "<v_OnFor>4,${Timer_4 ?: 0}</v_OnFor>"
    cmdRelay "<v_OnFor>5,${Timer_5 ?: 0}</v_OnFor>"
    cmdRelay "<v_OnFor>6,${Timer_6 ?: 0}</v_OnFor>"
    cmdRelay "<v_OnFor>7,${Timer_7 ?: 0}</v_OnFor>"
    cmdRelay "<v_OnFor>8,${Timer_8 ?: 0}</v_OnFor>"
    refresh()
}

def off() {
	//sendEvent(name: "switch", value: "off", displayed: true)
	cmdRelay "<v_Off>0</v_Off>"
    refresh()
}


def checkTime(t) {
	def time = (t ?: 0).toInteger()
    time > 60 ? 60 : time
}

def rainDelayed() {
    log.info "rain delayed"
    if(device.currentValue("switch") != "on") {
        sendEvent (name:"switch", value:"rainDelayed", displayed: true)
    }
}

//Update Pump When Applied on Particle Photon
def enablePump() 
{
    log.info "Pump Enabled"
    //gbee.smartShield(text: "pump,3").format()  //pump is queued and ready to turn on when zone is activated
}
def disablePump() 
{
    log.info "Pump Disabled"
    //gbee.smartShield(text: "pump,0").format()  //remove pump from system, reactivate Zone8
}
def onPump() 
{
    log.info "Pump On"
    //gbee.smartShield(text: "pump,2").format()
}

def offPump() 
{
	log.info "Pump Enabled"
    //gbee.smartShield(text: "pump,1").format()  //pump returned to queue state to turn on when zone turns on
}
def push() 
{
    log.info "advance to next zone"
   	cmdRelay "<v_Advance>0</v_Advance>"
    refresh()
}


// skip one scheduled watering
def	skip() 
{
    def evt = createEvent(name: "effect", value: "skip", displayed: true)
    log.info("Sending: $evt")
    sendEvent(evt)
}
// over-ride rain delay and water even if it rains
def	expedite() 
{
    def evt = createEvent(name: "effect", value: "expedite", displayed: true)
    log.info("Sending: $evt")
    sendEvent(evt)
}

// schedule operates normally5882798
def	noEffect() 
{
    def evt = createEvent(name: "effect", value: "noEffect", displayed: true)
    log.info("Sending: $evt")
    sendEvent(evt)
}

// turn schedule off indefinitely
def	onHold() 
{
    def evt = createEvent(name: "effect", value: "onHold", displayed: true)
    log.info("Sending: $evt")
    sendEvent(evt)
}




//Command and Reponse Functions
private cmdRelay(msg) {
    //Spark Core API Call
	httpPost(
		uri: "https://api.spark.io/v1/devices/${deviceId}/setCmd",
        body: [access_token: token, command: msg],
	) {response -> log.debug (response.data)}
}

//Get status response and parse
private getResp(String sparkVar) {
    //Spark Core API Call
    def readingClosure = { response ->
	  	log.debug "Reading request was successful, $response.data.result"
        str_parse(response.data.result)
	}
    httpGet("https://api.spark.io/v1/devices/${deviceId}/${sparkVar}?access_token=${token}", readingClosure)  
}

// Generic to get variable value
private getVar(String device_name,String sparkVar) {
    //Spark Core API Call
    def readingClosure = { response ->
	  	log.debug "Reading request was successful, $response.data.result"
      	sendEvent(name: device_name, value: response.data.result)
	}
    httpGet("https://api.spark.io/v1/devices/${deviceId}/${sparkVar}?access_token=${token}", readingClosure)
}

