
n = 8


#Print out the inputs
for i in range(1,n+1):
    print('input("Timer_%d", "text", title: "Zone %d", description: "Zone %d Time", required: false)' % (i,i,i))

#Print command list
for i in range(1,n+1):
    print('command "RelayOn%d"; command "RelayOnFor%d"; command "RelayOff%d"'%(i,i,i))

#Print out the tiles
for i in range(1,n+1):
    print('standardTile("zone_%d_Tile", "device.zone_%d", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true)' %(i,i))
    print('{')
    print('\tstate "off%d", label: "%d", action: "RelayOn%d", icon: "st.Outdoor.outdoor12", backgroundColor: "#ffffff",nextState: "sending%d"'%(i,i,i,i))
    print('\tstate "sending%d", label: "sending", action: "RelayOff%d", icon: "st.Health & Wellness.health7", backgroundColor: "#cccccc"'%(i,i))
    print('\tstate "q%d", label: "%d", action: "RelayOff%d",icon: "st.Outdoor.outdoor12", backgroundColor: "#c0a353", nextState: "sending%d"'%(i,i,i,i))
    print('\tstate "on%d", label: "%d", action: "RelayOff%d",icon: "st.Outdoor.outdoor12", backgroundColor: "#53a7c0", nextState: "sending%d"'%(i,i,i,i))
    print('\tstate "sendingOff%d", label: "sending", action: "RelayOff%d", icon: "st.Health & Wellness.health7", backgroundColor: "#cccccc"'%(i,i))
    print('}')
    
    
#Print out the tiles list
s = ""
for i in range(1,n+1):
    s=s+'"zone_%d_Tile",'%i
    
print(s)

#Print zone checks
for i in range(1,n+1):
    print('if(device.currentValue("zone_%d") in ["r%d","q%d"]) return true;'%(i,i,i))
    
    
#Print function calls
for i in range(1,n+1):
    print('def RelayOn%d()\n{\n\tcmdRelay "<v_On>%d</v_On>"\n\trefresh()\n}' %(i,i,))
    print('def RelayOn%dFor(value) \n{\n\tvalue = checkTime(value)\n\tcmdRelay "<v_OnFor>%d,$value</v_OnFor>"\n\trefresh()\n}' % (i,i))
    print('def RelayOff%d() \n{\n\tcmdRelay "<v_Off>%d</v_Off>"\n\trefresh()\n}' % (i,i))
    
#Print to turn all on

for i in range(1,n+1):
    print('cmdRelay "<v_OnFor>%d,${Timer_%d ?: 0}</v_OnFor>"'%(i,i))
