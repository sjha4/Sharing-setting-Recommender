# Context Aware Sharing
We have implemented our Context Aware Sharing Recommender with a persistent storage SQL Database. We also need a
adaptation coefficient (α) which will decide the depth of adaptation for our SIPA.

## How to use the Application
1. We have an application based on the Play framework.
You can run the project using sbt. (Install the latest version of sbt:
http://www.scala-sbt.org/download.html )
2. We use a SQLITE database for persistent storage. The database with the schema
and seed data is included in the zipped folder. It should work without any issues.
We are expecting a clean slate of checkins so the check-in table is empty for now
and starts populating upon use of the application.
3. Unzip the application folder and go to the directory in your console and hit
“ sbt run ”
4. The application should start when you go to http://localhost:9000 . (You need to hit
this URL to start)

## Components of the Application:
### PRIMARY STAKEHOLDER:
i) Suggest Check-in: 
The check-in can be done on the home page of the application. 
The companion Ids need to be user Ids separated by ‘|’ . Hit on submit to create the
checkin.
Upon checkin, all checkins are stored in the persistent storage for future SIPA algorithm.

ii) Retrieve Suggestions and Set Final Policy:
Every 10 seconds, there is a task scheduled to retrieve all suggestions by compnions on the
suggested check-in. Out of all the suggestions ( available at the time of processing) , we
discrad suggestions which demote arguments that are in our promote list.

2
Out of the suggestions that do not demote our initial promote arguments, we take
into account the argument and policy that is suggested by the most number of users
(including own SIPA’s arguments).
Once the reasoning phase is complete, the final policy decided is set on the suggested
checkin.

### SECONDARY STAKEHOLDER:

i) Get Checkins to suggest And Respond:
We have a task every 5 seconds that fetches all the suggested checkins where we need to
provide suggestions as secondary stakeholder. We process the fetched check-ins we need
to respond to and provide our own suggested policy along with supporting policy
arguments.

2. Retrieve Feedback:
We have a scheduled task every 30 seconds which fetches the feedback received on the
checkins created by the user.
We have a persistent storage which stores the check-in details. We store the sanctions
received associated with the check-ins.

3. Provide Sanction:
We have a scheduled task which runs every 30 seconds which fetches all unsanctioned
check-ins the user has been tagged in.
For every checkin, we run a review code which decides whether the sharing policy used
here matches any of the commitments, goals or adaptive policies of the user.

4. Adaptive Policy Making:
Whenever the user needs to check-in to a place with some companions, we ascertain the
policy to be adopted based on a number of factors
