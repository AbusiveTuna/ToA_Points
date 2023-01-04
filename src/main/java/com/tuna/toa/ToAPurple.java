import static net.runelite.client.RuneLite.RUNELITE_DIR;

public class ToAPurple 
{
  private userName;
  private List <ToARaid> raidList = new ArrayList<ToARaid>();
  private Map purplesMap = new HashMap();
  
  final private String filePath = RUNELITE_DIR + "/" + userName + "/ToAPointTracker/ToACompletions.txt";
  
    public void setUp()
    {
      
        String dir = RUNELITE_DIR + "/" + userName + "/ToAPointTracker";

        Files.createDirectories(Paths.get(dir));

        File file = new File(filePath);
      
    }
  
    public void saveRaid()
    {
          setup();
       
          String invocationLevel = ToAPlugin.getInvocationLevel();
          String purpleChance = ToAOverlay.getPurpleChance();
          String completionTime = ToAPlugin.getTimeCompleted();
          String partySize = ToAPlugin.getPartySize();
      
          File file = new File(filePath);
          FileWriter fr = new FileWriter(file, true);
          BufferedWriter br = new BufferedWriter(fr);
          br.write("Invocation Level: " + invocationLevel + "\n");
          br.write("Purple Chance: " + purpleChance + "\n");
          br.write("Time to complete: " + completionTime + "\n");
          br.write("Party Size: " + partySize + "\n");
    }
  
    public void loadSavedRaids()
    {
      File file = new File(filePath);
      
      BufferedReader br = new BufferedReader(new FileReader(file));
      
      String line;
      while ((line = br.readLine()) != null)
      {
          ToARaid currentRaid = new ToARaid();
          if(line.contains("Invocation Level")){
            String[] invoString = line.split(":");
            currentRaid.invocationLevel = invoString[1].trim();
          }
          if(line.contains("Purple Chance"){
            String[] purpleChanceString = line.split(":");
            currentRaid.purpleChance = purpleChanceString[1].trim();
          }
          if(line.contains("Time to complete"){
            String[] timeToCompleteString = line.split(":");
            currentRaid.timeToComplete = timeToCompleteString[1].trim();
          }
          if(line.contains("Party Size"){
            String[] partySizeString = line.split(":");
            currentRaid.partySize = partySizeString[1].trim();
          }
          raidList.add(currentRaid);
      }
    }
  
    public void fillMap()
    {
      loadSavedRaids();

      for(int i = 0; i < raidList.length(); i++)
      {
          ToARaid currentRaid = raidList[i];
          String invo = currentRaid.invocationLevel;
          String purpleChance = currentRaid.purpleChance;
          String timeToComplete = currentRaid.timeToComplete;
          
          String[] time = timeToComplete.split(".");
          double timeInSeconds = 0.0;
          if(time.length = 2){
            double minutes = Double.valueOf(time[0]);
            double seconds = Double.valueOf(time[1]);
            
            timeInSeconds = (minutes * 60) + seconds;

          }
          else{
           double hours = Double.valueOf(time[0]);
           double minutes = Double.valueOf(time[1]);
           double seconds = Double.valueOf(time[2]);
            
            timeInSeconds = (hours * 3600)+ (minutes * 60) + seconds;
           
          }
          double purpsPerSecond = Double.parseDouble(purpleChance) / timeInSeconds;
          
        
          //at the end we should have a map that looks like this.
          //Key: 150, Values: [.0000312,.00003222] etc etc.
          if(purplesMap.containsKey(invo)){ // map already has this invocation level
            List <double> ppsList = purplesMap.get(invo);
            ppsList.add(purpsPerSecond);
            purplesMap.put(invo,ppsList);
          }
          else{
            List <double> ppsList = new ArrayList<double>();
            ppsList.add(purpsPerSecond);
            purplesMap.put(invo, ppsList);
          }
        
//            if(purplesMap.containsKey(invo + "Time")){ // map already has this invocation level
//             List <double> timeList = purplesMap.get(invo + "Time");
//             timeList.add(timeInSeconds);
//             purplesMap.put(invo,timeList);
//           }
//           else{
//             List <double> timeList = new ArrayList<double>();
//             timeList.add(timeInSeconds);
//             purplesMap.put(invo+"Time", timeList);
//           }
        
        
      }
    }
        
    public void calculate(){
      
      for (Map.Entry<String, List <double>> entry : map.entrySet()) {
        String key = entry.getKey();
        List <double> ppsList = entry.getValue();
        double averagePPS;
        for(int i = 0; i < ppsList.length(); i++){
               averagePPS = averagePPS + ppsList.get(i);
          }
          averagePPS = averagePPS / ppsList.length();
          
          secondsForPurple = 100/averagePPS;
          int hours, minutes, seconds = 0;
          if(secondsForPurple > 3599){ //hours
            hours = secondsForPurple/3600;
            secondsForPurple = secondsForPurple - (3600 * hours);
          }
          if(secondsForPurple > 59){ //minutes
            minutes = secondsForPurple/60;
            secondsForPurple = secondsForPurple - (60* minutes);
          }
        
          System.out.println("At invocation level: " + key + " It would take on average " + hours + ":" + minutes + "." + seconds " for a purple.");
        }
      
    }
  
}
