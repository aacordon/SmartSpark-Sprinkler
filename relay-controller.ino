
#define nRelays 8
#define nValves 8
#define relayMode 0 //0 for sequentual, 1 for conccurrent

// Define Onbaord LED for testing
int led = D7; 

//Time Variables
double start_time=0;
double end_time=0;
double curr_time=0;
int wifi_rssi = 0;

//Status Strings
char currLEDState[250];
char rtrnStr[250];
char statusStr[250];

char v_state[250];
//char wifi_rssi[25];

//Relay Arrays
int r_cmd[nRelays];
int r_queue[nRelays];
int r_times[nRelays];
int r_curr = 0;

//Valve Variables
int v_cmd[nValves];
int v_queue[nValves];
int v_queue_cnt=0;
int v_times[nValves];
int v_curr = 0;
bool v_start = false;
double v_time_end = 0;

void setup() 
{
  //Register Spark function
  Particle.function("setCmd", parseCmd);
  Particle.variable("currTime", &curr_time, DOUBLE);
  Particle.variable("wifiStrength", &wifi_rssi, INT); 
  
  Particle.variable("currLEDState", currLEDState, STRING);
  Particle.variable("rtrnStr", rtrnStr, STRING);
  Particle.variable("statusStr", statusStr, STRING);
  Particle.variable("v_state", v_state, STRING);
  
  //Sync Time
  Particle.syncTime();
  Time.zone(-7);
  
  // Initialize D0 pin as an output
  pinMode(led, OUTPUT);
}

void loop() {
    sprinkler_controller();
    status();
    //sprintf(strBuffer,"Time:%5.0f",getTime());
}

void status()
{
    //sprintf(wifi_rssi,"%03d dB\nRSSI", WiFi.RSSI());
    wifi_rssi= WiFi.RSSI();
    sprintf(v_state,"");
    for (int valve=1;valve<nValves+1;valve++)
    {
        if(valve==v_curr)
        {
            sprintf(v_state,"%son%d",v_state,valve);
        } else {
            if(v_in_q(valve) == 1)
            {
                sprintf(v_state,"%sq%d",v_state,valve);
            } else {
                sprintf(v_state,"%soff%d",v_state,valve);
            }
        }
        
        if (valve < nValves)
            sprintf(v_state,"%s,",v_state);
    }
}

void sprinkler_controller()
{
    //Handle Single Sprinkler On
   if(isVcmd() > 0 && v_queue_cnt == 0) 
   {
       v_curr = isVcmd();
       sprintf(rtrnStr,"v%d: On",v_curr);
       
       //Send Command to Start Valve with mask
   }

    //Handle Queued Sprinklers
   if(isVcmd() > 0 && v_queue_cnt > 0 ) 
   {
       v_curr = v_queue[0];
       
       if(!v_start)
       {
            v_start=true;
            v_time_end = (getTime()+60*v_times[v_curr-1]);
            if (v_time_end >= 86400)
                v_time_end=0;
                

           //Send Command to Start Valve
       } else {
           
           sprintf(rtrnStr,"v%d: On, C:%5.0f, E:%5.0f ,%5.0f s left",v_curr,getTime(),v_time_end,v_time_end-getTime());
           
           if(getTime()>=v_time_end) 
           {
               //Send Command to Stop Current Valve
               sprintf(rtrnStr,"v%d:Stopped",v_curr);
               //Reset Variables
               v_start = false;
               v_queue_cnt--;
               v_queue_cl_n_shift();
               v_cmd[v_curr-1]=0;
               v_curr = 0;
           }
       }
   }
   
   if(isVcmd()==0)
   {
       //Send Command to Turn off All Valves and reset parameters for good measure
       sprintf(rtrnStr,"All Off");
       v_curr = 0;
   }
}




int parseCmd(String data) {
    int relay = 0;
    int valve = 0;
    
    //Led Control
    String ledState = tryExtractString(data, "<ledState>", "</ledState>");
    
    
    //Relay Control
    //Return the Relay State
    String r_State = tryExtractString(data, "<r_State>", "</r_State>");
    //Direct Relay Control
    String r_On = tryExtractString(data, "<r_On>", "</r_On>");
    //Direct Relay Control
    String r_Off = tryExtractString(data, "<r_Off>", "</r_Off>");    
    //Timed Relay Control
    String r_OnFor = tryExtractString(data, "<r_OnFor>", "</r_OnFor>");
    
    
    //Water Valve Control
    //Return the Valve State
    String v_State = tryExtractString(data, "<v_State>", "</v_State>");
    //Direct Valve Control
    String v_On = tryExtractString(data, "<v_On>", "</v_On>");
    //Direct Valve Control
    String v_Off = tryExtractString(data, "<v_Off>", "</v_Off>");    
    
    //Timed Relay Control
    String v_OnFor = tryExtractString(data, "<v_OnFor>", "</v_OnFor>");    
    //Advancing valves
    String v_Advance = tryExtractString(data, "<v_Advance>", "</v_Advance>");
    
    
    //Led Control
    if (ledState != NULL) {
        if (ledState == "ON") {
            digitalWrite(led, 1);
            sprintf(currLEDState,"State is: on");
        }
        else {
            digitalWrite(led, 0);
            sprintf(currLEDState,"State is: off");
        }
        curr_time = getTime();
    }
    
    //Relays Functions
    //Set the status of the relay in the string
    if (r_State != NULL) {
        relay = atoi(r_State);
        if (relay != r_curr && r_cmd[relay-1]==1)
            sprintf(statusStr,"R%d: In Queue",relay);

        if (relay != r_curr && r_cmd[relay-1]==0)
            sprintf(statusStr,"R%d: Off",relay);

        if (relay = r_curr && r_cmd[relay-1]==1)
            sprintf(statusStr,"R%d: On",relay);

        if (relay = r_curr && r_cmd[relay-1]==0)
            sprintf(statusStr,"R%d: Off",relay);
    }
    //Turn relay on
    if (r_On != NULL) {
        relay = atoi(r_On);
        r_cmd[relay-1] = 1;
    }
    //Turn relay off
    if (r_Off != NULL) {
        relay = atoi(r_Off);
        if (relay > 0)
            r_cmd[relay-1] = 0;
        else
            memset(v_cmd,0,nValves);
    }
    //Turn relay on for a set time period
    if (r_OnFor != NULL) {
        const char* stringArgs = r_OnFor.c_str();
        char* myCopy = strtok(strdup(stringArgs), ",");
        relay = atoi(myCopy);
        if (relay > 0) {
            myCopy = strtok(NULL, ","); 
            r_cmd[relay-1] = 1;
            r_times[relay-1] = atoi(myCopy);
        }
    } 
    
    //Valve Functions
    //Set the status of the valve in the string
    if (v_State != NULL) {
        valve = atoi(v_State);
        if (valve != v_curr && v_cmd[valve-1]==1)
        {
            sprintf(statusStr,"V%d: In Queue",valve);
            sprintf(v_state,"q%d",valve);
        }
        if (valve != v_curr && v_cmd[valve-1]==0)
        {
            sprintf(statusStr,"V%d: Off",valve);
            sprintf(v_state,"off%d",valve);
        }
        if (valve = v_curr && v_cmd[valve-1]==1)
        {
            sprintf(statusStr,"V%d: On",valve);
            sprintf(v_state,"on%d",valve);
        }
        if (valve = v_curr && v_cmd[valve-1]==0)
        {
            sprintf(statusStr,"V%d: Off",valve);
            sprintf(v_state,"off%d",valve);
        }

    }
    //Turn specific valve on -- Turn any others off if on, and kill queue
    if (v_On != NULL) {
        valve = atoi(v_On);
        v_clr();
        v_cmd[valve-1]=1;
        digitalWrite(led, 1);
    }
    //Turn specific valve off
    if (v_Off != NULL) {
        valve = atoi(v_Off);
        if (valve == 0)
        {
            //Reset
            v_clr();
        } else {
            v_cmd[valve-1] = 0;
        }
        digitalWrite(led, 0);
    }
    //Turn valve on for a set time period -- Use queues when called for various valves
    if (v_OnFor != NULL) {
        const char* stringArgs = v_OnFor.c_str();
        char* myCopy = strtok(strdup(stringArgs), ",");
        valve = atoi(myCopy);
        if (valve > 0) {
            myCopy = strtok(NULL, ",");
            
            //Use Queues
            if(!v_in_q(valve))
            {
                if (v_queue_cnt==0)
                    v_clr();
                    
                v_queue_cnt++;
                
                if (v_queue_cnt < nValves+1)
                {
                    v_queue[v_queue_cnt-1]=valve;
                    v_cmd[valve-1] = 1;
                    v_times[valve-1] = atoi(myCopy);
                    
                    sprintf(statusStr,"V%d: Added to Queue, Position %d (%d,%d)",valve,v_queue_cnt,isVcmd(),v_in_q(valve));
                    
                } else {
                    sprintf(statusStr,"V%d: Queue Full",valve);  
                }                
            } else {
                sprintf(statusStr,"V%d: Already in Queue, Time Updated",valve);  
                v_times[valve-1] = atoi(myCopy);
            }

        }
    }     
    
    //Shift Valves
    if (v_Advance != NULL)
    {
        //Determine wether in queue mode or single
        if(v_queue_cnt==0)
        {
            if (isVcmd() > 0) 
            {
                valve = isVcmd();
                if (valve == nValves)
                {
                    valve = 1;
                    valve_tgl_on(valve); 
                } else {
                    valve++;
                    valve_tgl_on(valve);
                }

            }
            sprintf(statusStr,"Advancing to V%d",valve); 
        } else {
            //Trick it by setting a new end time
            v_time_end=getTime();
            if(v_queue[1] > 0)
                sprintf(statusStr,"Advancing to V%d",v_queue[1]); 
            else
                sprintf(statusStr,"No More In Queue"); 
        }
        
    }
    
return 1;
}

void v_clr()
{
    for(int i=0;i<nValves;i++)
    {
        v_cmd[i]=0;
        v_queue[i]=0;
        v_queue_cnt = 0;
        v_curr = 0;
        v_start = false;
    }
}

int isVcmd(void)
{
    int valve = 0;
    for(int i = 0; i < nValves; i++)
    {
        if(v_cmd[i] > 0)
        {
            valve = i+1;
            break;
        }
            
    }    
return valve;    
}


int v_in_q(int valve)
{
    int check_fl = 0;
    for(int i = 0; i < nValves; i++)
    {
        if(v_queue[i] == valve)
            check_fl = 1;
    }    
return check_fl;
}

void v_queue_cl_n_shift()
{
    for (int i=1; i < nValves; i++) 
    {
        v_queue[i-1]=v_queue[i];
    }
    v_queue[nValves-1] = 0;
}

void valve_tgl_on(int valve)
{
    /*
    if (v_curr > 0)
        sprintf(v_state,"on%d,off%d",valve,v_curr);
    else
        sprintf(v_state,"on%d",valve);
        */
    for(int i = 0; i < nValves; i++)
    {
        if(valve == i+1)
            v_cmd[i]=1;
        else
            v_cmd[i]=0;
    }
    
}


//Get the current time in seconds
double getTime() 
{
    return (Time.hour()*3600+Time.minute()*60+Time.second());
}

// Returns any text found between a start and end string inside 'str'
// example: startfooend  -> returns foo
String tryExtractString(String str, const char* start, const char* end) {
    if (str == NULL) {
        return NULL;
    }

    int idx = str.indexOf(start);
    if (idx < 0) {
        return NULL;
    }

    int endIdx = str.indexOf(end);
    if (endIdx < 0) {
        return NULL;
    }

    return str.substring(idx + strlen(start), endIdx);
}