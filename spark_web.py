import urllib.request, urllib.parse
import time
import json

class sparkConnect():
    def __init__(self,deviceID = None, access_token = None):
        self.deviceID = deviceID
        self.access_token = access_token
        self.baseURL = 'https://api.spark.io/v1/devices'
        
    def sparkPost(self,fcn = None,msg = None):
        
        url = '%s/%s/%s' % (self.baseURL,self.deviceID,fcn)
        data = {
        'access_token' : self.access_token,
        'command' : msg
        }
        
        data = bytes( urllib.parse.urlencode( data ).encode())
        try:
            handler = urllib.request.urlopen( url, data )
            resp = json.loads(handler.read().decode( 'utf-8' ))
            return 1
        except Exception as e:
            print(e)
            return 0
    
    def sparkVar(self,var = None):
        resp = {}
        url = '%s/%s/%s?access_token=%s' % (self.baseURL,self.deviceID,var,self.access_token)
        try:
            handler = urllib.request.urlopen( url )
            resp = json.loads(handler.read().decode( 'utf-8' ))
            print(resp['result'])
            return resp['result']
        except Exception as e:
            print(e)
            return None
        



#Device Information
deviceId = ""
access_token = ""

dev1 = sparkConnect(deviceId,access_token)

'''
dev1.sparkPost("setCmd","<ledState>ON</ledState>")
dev1.sparkVar("currLEDState")

dev1.sparkVar("currTime")
dev1.sparkPost("setCmd","<ledState>OFF</ledState>")
dev1.sparkVar("currLEDState")

dev1.sparkPost("setCmd","<r_On>1</r_On>")
dev1.sparkPost("setCmd","<r_State>1</r_State>")  #Publish the updated results to State Var
dev1.sparkVar("strBuffer")

dev1.sparkPost("setCmd","<r_Off>1</r_Off>")
dev1.sparkPost("setCmd","<r_State>1</r_State>")  #Publish the updated results to state Var
dev1.sparkVar("strBuffer")


dev1.sparkPost("setCmd","<v_OnFor>1,1</v_OnFor>")
dev1.sparkPost("setCmd","<v_OnFor>3,1</v_OnFor>")
dev1.sparkPost("setCmd","<v_OnFor>2,1</v_OnFor>")
dev1.sparkPost("setCmd","<v_OnFor>6,1</v_OnFor>")
dev1.sparkPost("setCmd","<v_OnFor>7,1</v_OnFor>")
dev1.sparkPost("setCmd","<v_OnFor>4,1</v_OnFor>")
dev1.sparkPost("setCmd","<v_OnFor>5,1</v_OnFor>")
dev1.sparkPost("setCmd","<v_OnFor>8,1</v_OnFor>")
dev1.sparkPost("setCmd","<v_OnFor>5,1</v_OnFor>")


dev1.sparkVar("rtrnStr")
dev1.sparkPost("setCmd","<v_State>1</v_State>")  #Publish the updated results to State Var
dev1.sparkVar("rtrnStr")


dev1.sparkPost("setCmd","<v_On>2</v_On>")
dev1.sparkVar("rtrnStr")
time.sleep(5)

dev1.sparkPost("setCmd","<v_On>3</v_On>")
dev1.sparkVar("rtrnStr")
time.sleep(5)

dev1.sparkPost("setCmd","<v_Off>3</v_Off>")
dev1.sparkVar("rtrnStr")
time.sleep(5)


dev1.sparkPost("setCmd","<v_OnFor>5,1</v_OnFor>")
dev1.sparkVar("statusStr")
dev1.sparkPost("setCmd","<v_OnFor>1,1</v_OnFor>")
dev1.sparkVar("statusStr")
dev1.sparkPost("setCmd","<v_OnFor>3,1</v_OnFor>")
dev1.sparkVar("statusStr")
dev1.sparkPost("setCmd","<v_OnFor>6,1</v_OnFor>")
dev1.sparkVar("statusStr")
dev1.sparkVar("rtrnStr")
time.sleep(5)
dev1.sparkVar("rtrnStr")
dev1.sparkPost("setCmd","<v_Advance>0</v_Advance>")
dev1.sparkVar("statusStr")
time.sleep(5)
dev1.sparkVar("rtrnStr")


dev1.sparkPost("setCmd","<v_OnFor>1,1</v_OnFor>")
dev1.sparkVar("statusStr")
dev1.sparkPost("setCmd","<v_OnFor>2,1</v_OnFor>")
dev1.sparkVar("statusStr")
dev1.sparkPost("setCmd","<v_OnFor>6,1</v_OnFor>")
dev1.sparkVar("statusStr")
dev1.sparkPost("setCmd","<v_OnFor>4,1</v_OnFor>")
dev1.sparkVar("statusStr")
'''
for i in range(0,100):
    dev1.sparkVar("rtrnStr")
    dev1.sparkVar("v_state")
    dev1.sparkVar("wifiStrength")
    time.sleep(5)

