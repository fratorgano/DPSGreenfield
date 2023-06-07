# DPSGreenfield
### Project for Distributed and Pervasive Systems course
Distributed mutual exclusion algorithm: Ricart-Agrawala
### Why are some maintenance information sent to the server?
It was done for visualization purposes only.

I created a webpage that uses the REST information provided by the server 
to visualize the status of the system, but I also wanted to visualize the maintenance
status of the system and, since that information was only inside the various nodes, I added
* A rest PUT request for robots to notify that they are joining maintenance
* A rest PUT request for robots no notify that they are leaving maintenance

#### Steps
1. Robot needs maintenance
2. Applies Ricart-Agrawala algorithm to get access to the resource
3. After getting all Oks to go ahead and use maintenance:
   1. Notifies the server that it's going in maintenance
   2. Goes to maintenance
   3. Notifies the server that it's leaving maintenance
   4. Leaves maintenance
