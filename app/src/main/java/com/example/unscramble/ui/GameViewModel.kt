package com.example.unscramble.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.unscramble.data.allWords
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.update
import com.example.unscramble.data.SCORE_INCREASE
import com.example.unscramble.data.MAX_NO_OF_WORDS

class GameViewModel : ViewModel() {
    // Game UI state
    // Backing property to avoid state updates from other classes
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    var userGuess by mutableStateOf("")                                                             //Compose observes mutableStateOf("") value and sets the initial value to ""
        private set

    // Set of words used in the game
    private var usedWords: MutableSet<String> = mutableSetOf()                                      //Stores used words so that they won't be used again
    private lateinit var currentWord: String                                                        //Saves the current scrambled word

    private fun pickRandomWordAndShuffle(): String {                                                //Helper method to pick a random word from the list and return as a String
        // Continue picking up a new random word until you get one that hasn't been used before
        currentWord = allWords.random()
        if (usedWords.contains(currentWord)) {
            return pickRandomWordAndShuffle()
        } else {
            usedWords.add(currentWord)
            return shuffleCurrentWord(currentWord)
        }
    }

    private fun shuffleCurrentWord(word: String): String {                                          //Helper method to shuffle the current word that takes a String and returns the shuffled String
        val tempWord = word.toCharArray()
        // Scramble the word
        tempWord.shuffle()
        while (String(tempWord).equals(word)) {
            tempWord.shuffle()
        }
        return String(tempWord)
    }

    fun resetGame() {                                                                               //Helper function to initialize the game
        usedWords.clear()                                                                           //Clears away used words so that they may be used again in a new game
        _uiState.value = GameUiState(currentScrambledWord = pickRandomWordAndShuffle())             //Initialize the _uiState and pick a new word for currentScrambledWord using pickRandomWordAndShuffle()
    }

    init {                                                                                          //Root process to start app
        resetGame()
    }

    fun updateUserGuess(guessedWord: String){                                                       //Method updates the user's guess with the passed in guessWord
        userGuess = guessedWord
    }

    fun checkUserGuess() {                                                                          //Verify the user's guess is the same as the currentWord

        if (userGuess.equals(currentWord, ignoreCase = true)) {                                     //If user's guess is correct, increase the score
            // User's guess is correct, increase the score
            // and call updateGameState() to prepare the game for next round
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updatedScore)                                                           //Calls the updateGameState function to increase the score
        }
        else {
            // User's guess is wrong, show an error
            _uiState.update { currentState ->
                currentState.copy(isGuessedWordWrong = true)
            }
        }
        // Reset user guess
        updateUserGuess("")
    }

    private fun updateGameState(updatedScore: Int) {                                                //Update the score, increment the current word count and pick a new word from the WordsData.kt file
        if (usedWords.size == MAX_NO_OF_WORDS){                                                     //Checks if the usedWords size is equal to MAX_NO_OF_WORDS
            //Last round in the game, update isGameOver to true, don't pick a new word
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    score = updatedScore,
                    isGameOver = true                                                               //Indicates the end of the game
                )
            }
        }
        else{
            // Normal round in the game
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    currentScrambledWord = pickRandomWordAndShuffle(),
                    currentWordCount = currentState.currentWordCount.inc(),                         //increases the word count
                    score = updatedScore
                )
            }
        }
    }

    fun skipWord() {                                                                                 //Skip button
        updateGameState(_uiState.value.score)
        // Reset user guess
        updateUserGuess("")
    }
}