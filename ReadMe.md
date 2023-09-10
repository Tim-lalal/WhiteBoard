
# Multi-Threaded Shared Whiteboard

This project provides an implementation of a shared whiteboard, allowing multiple users to draw on the same canvas concurrently. It showcases the application of multi-threading in Java.


https://github.com/Tim-lalal/WhiteBoard/assets/80299732/7787ab93-5e21-40ff-814a-3cc5c85b03f0




## Table of Contents

- [Features](#features)
- [Technologies Used](#technologies-used)
- [Getting Started](#getting-started)
- [Usage](#usage)
- [License](#license)
- [Contact](#contact)

## Features

- **Real-time Drawing**: Users can see each other's drawings in real-time.
- **Multi-Threading**: Efficient handling of multiple users drawing at the same time.
- **Easy-to-Use Interface**: Intuitive UI/UX for a better user experience.
- **RealTime-ChatBox**: Log in users can chat at anytime with the others.
- **Color & Tool Selection**: Various drawing tools and colors for users to choose from.

## Technologies Used

- **Java**: The primary programming language for the back-end.
- **JavaAWT**: Using AWT to implement user graphical interface
- **Sockets**: Using Sockets to implement the real time user join in and data transfer.

## Getting Started

### Prerequisites

- Java JDK 17
- gson-2.10.1

### Installation

1. Clone the repository:
   ```bash
[   git clone https://github.com/yourusername/shared-whiteboard.git](https://github.com/Tim-lalal/WhiteBoard.git)
   ```

2. Navigate to the project directory:
   ```bash
   cd whiteBoard
   ```

3. Run the program:

   ```bash
   method 1: Running the src/main/manager/Server.java first.
             Running the src/main/client/ClientLoginWindow.java(ClientLoginWindow2.java).
   method 2: In the out/artifacts/dsassignment2_jar(dsassignment2_jar2. Running the CreateWhiteBoard.jar,JoinWhiteBoard.jar one by one.
   ```

## Usage

1. Start the server.
2. Launch the client whiteboard.
3. waiting for server permission.
4. Connect to the shared whiteboard.
5. Begin drawing and watch as other users join and draw concurrently.


## License

This project is licensed under the [MIT License](link-to-your-license-file).

## Contact

For any inquiries or feedback, please reach out to:
- Email: www.liusky@gmail.com
