So this is honestly more of a thought but, i've been thinking of adding something that can display your optimal raid level.

When a user finishes a raid, we can do the following:
Save their invocation level and map it to a purple chance.
Save their time.
Use a calculation i've been calling "ppm" or purples per minute and display how long it would take to get a purple.

Thinking of it like the cox rewards plugin display:

150: Runs, Average Purple Chance, Average Time, Time per purple, Raids per purple
200: Runs, Average Purple Chance, Average Time, Time per purple, Raids per purple
250: Runs, Average Purple Chance, Average Time, Time per purple, Raids per purple

so on and so on.

Possibly a way to display by groups?

Sample code in javascript:
invo mapping (invo,purpleChance)
150,2.02
160,2.11
170,2.20
180,2.31
190,2.42
200,2.53
210,2.65
220,2.78
230,2.92
240,3.07
250,3.23
260,3.40
270,3.59
280,3.79
290,4.01
300,4.25


Hello. My purpose is to find the best invocation level for you.
At 150 invocation it would take: 20 hours and 37 minutes to obtain a purple or 47 raids at that level

Hello. My purpose is to find the best invocation level for you.
At 200 invocation it would take: 23 hours and 42 minutes to obtain a purple or 38 raids at that level

Hello. My purpose is to find the best invocation level for you.
At 250 invocation it would take: 20 hours and 38 minutes to obtain a purple or 30 raids at that level

Hello. My purpose is to find the best invocation level for you.
At 300 invocation it would take: 19 hours and 36 minutes to obtain a purple or 23 raids at that level

// Online Javascript Editor for free
// Write, Edit and Run your Javascript code using JS Online Compiler

console.log("Hello. My purpose is to find the best invocation level for you.")


let time = "50:00";
let invo = 300;
let purpleChance = 4.25;

const timeArray = time.split(":");

let convertedTime = parseInt(timeArray[0]);
if(parseInt(timeArray[1]) > 30){
    convertedTime++;
}



let ppm = purpleChance/convertedTime
let mod = ppm;
let runPPM = ppm;
let minutes = 0;
let hours = 0;
let runs = 0;
while(ppm < 100){
    ppm = ppm + mod;
    runPPM = runPPM + mod;
    minutes++;
    if(minutes == 60){
        hours++;
        minutes = 0;
    }
    if(runPPM > purpleChance){
        runs++;
        runPPM = 0;
    }
}


console.log("At " + invo + " invocation it would take: " + hours + " hours and " + minutes + " minutes to obtain a purple or " + runs + " raids at that level");
