# A. Assignment Information

-Project name: Tic Tac Toe

-Name: Dhaval Pungaliya

-ID No: 2018B2A70662G

-Email: f20180662@goa.bits-pilani.ac.in


# B.App:
## 1)Description

This app is a simple Tic-Tac-Toe game. The game has both single player and two player modes.

The users create an account and this is synced with a server.
 
The Statistics of the player(wins/losses/ties) are tracked using this account and are even displayed in the app.

In the single player mode,a person can play against a computer,and using the two player mode one can play against another human user who is using their own device and has the same app.


## 2)Known Bugs

Network connection issue can lead to bugs during the game. 

(i)Single player game- Eg: Player stats not updating after a game if the network access is lost in between the game and the app is closed/restarted before internet access is regained. 

(ii)Two player game- Eg: Player1 simply waiting for player2 to make their move if player2 has lost their network.


# C. Task Descriptions

I used Firebase as the backend for the app. All the user accounts, player stats, game details are stored and synced using Firebase(as shown in the modules).


## Task 1: Authentication, Dashboard and Creating New Games

I used the `Firebase Auth` module for authentication. It handles the password and email validation and manages user accounts on Firebase. I wrapped the user details, sign in and sign out functionality into a `ViewModel` that is shared over the activity. The signed in/signed out state is exposed as a `LiveData` that the Fragments observe. The navigation then is taken based on the changes in the observed value. The player stats (wins/losses/ties) are exposed using the `UserDetailsViewModel` as LiveData.

The 'Dashboard' has its own ViewModel that exposes the list of open games as a LiveData. This is observed and displayed on the `DashboardFragment` The `UserDetailsViewModel` is used to handle the sign out option in the DashboardFragment as well. The DashboardFragment too observes the login status LiveData from the UserDetailsViewModel and navigates back to the login page on sign out.

The `FloatingActionButton` on the `DashboardFragment` is used to create new games. A dialog is show on what type of game to create. The details on the type of game are passed to the `GameFragment` as arguments using SafeArgs.

## Task 2: Single Player Game

A `GameViewModel` was created to handle all the logic of the `GameFragment`. A game type independent `Game`  is used as the model for the game and is used by the GameViewModel. The `AIPlayer` class is used to create an instance of the local device based player(simply chooses a random empty location for each move). After each move, the status of the game is checked, if the game has ended, the appropriate dialogs are shown and the player stats are updated on the server.


## Task 3: Two Player Game

A player, the creator, creates a two player game on the server and the UID of this game is added to the open list of games on the server. The other player, the joiner, joins this game from the list of open games shown on their device. On joining, the joiner removes the game UID from the open games list and sets their user UID in the field for the joiner. This change informs the creator that the other player has joined and the game starts. Any move that is made is checked locally(to ensure that the no rules of the game are violated) and then the change is made in the server. These changes will be notified to the other player and the game goes on. Any win/loss/tie situation will be recognized locally based on the game state and code on each user's device will make the necessary updates to their respective stats.

# D. Running the App

There is setup required on the hosting side since Firebase is used as a backend and should always be running.
After running the app, enter an email(proper format) and password(more than 6 characters). If you have not created an account yet, hit `SIGN UP`, else hit `LOG IN`. Once the sign up or login is successful, you will be taken to the dashboard where you view your stats, create new games or join existing ones. 



# E. Testing and Accessibility

I did not look at the aspects of testing,as it was very time consuming and for this assignment,very subjective as well.

I took my friend,Karan Shah's help during this assignment. I also referred to a lot of online material for the same.

# F. Project Duration

I took around 60-70 hours to do this final project.

# G. Project Difficulty

Rating- 8/10
