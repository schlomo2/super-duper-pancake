# NQueens

A simple game, place N number of queens on an NxN board without threatening each other.

## Installation / testing

Clone this repository and import into Android Studio

**git clone git@github.com:schlomo2/NQueens.git**

- Tap a square to place a queen onto the or drag one from the available pile or another square.
- Tap the refresh icon in the top left to restart the game.
- Tap the settings icon in the top right to change the board size or enable move indicators

Win the game by placing all queens on the board without any conflicts.

## Architecture

Combines Hilt for dependency injection, DataStore for persistent key-value, and Jetpack Compose for building the UI.

1. UI Layer (Presentation Layer)
   Components: Composable functions, ViewModels.

2. Data Layer
   Components: Models, DataStore

3. Dependency Injection with Hilt
   Components: @HiltAndroidApp, @AndroidEntryPoint, @Inject, @Provides, @Module, @InstallIn