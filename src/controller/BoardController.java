package controller;

import java.io.IOException;

import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextArea;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Maze;
import model.Player;
import model.door.Door;
import model.door.GoldenDoor;
import model.door.SilverDoor;
import model.room.*;
import routes.Route;

public class BoardController {
    @FXML
    private GridPane board;

    @FXML
    private Label lblTokens;

    @FXML
    private Label lblRooms;

    @FXML
    private Text lblPlayerState;

    @FXML
    private Label lblTime;

    @FXML
    private JFXTextArea path;

    @FXML
    private ImageView key;

    @FXML
    public JFXProgressBar progress;

    private MazeController mController;
    private Player player;
    private boolean type;
    private int endPoint;
    private Stage modal;

    private final int EASY_V = 9;
    private final int MEDIUM_V = 16;
    private final int HARD_V = 25;

    public BoardController(MazeController mController, Player player, boolean type) {
        this.mController = mController;
        this.player = player;
        this.type = type;
    }

    public int getEndPoint() {
        return endPoint;
    }

    public Player getPlayer() {
        return player;
    }

    public void decreaseTime() {
        int currentTime = Integer.valueOf(lblTime.getText());
        if(currentTime!=0){
            lblTime.setText((currentTime - 1) + "");
        }
    }

    public void easyBoard() {
        endPoint = 3;
        mController.getMaze().createGraph(EASY_V, type);
        createRooms(EASY_V, 1);
        createDoors(endPoint, 1);
        createBoard(EASY_V, "green");
        lblTime.setText(EASY_V + "");
        setPlayer();
    }

    public void mediumBoard() {
        endPoint = 4;
        mController.getMaze().createGraph(MEDIUM_V, type);
        createRooms(MEDIUM_V, 2);
        createDoors(endPoint, 2);
        createBoard(MEDIUM_V, "blue");
        lblTime.setText(MEDIUM_V + "");
        setPlayer();
    }

    public void hardBoard() {
        endPoint = 5;
        mController.getMaze().createGraph(HARD_V, type);
        createRooms(HARD_V, 3);
        createDoors(endPoint, 3);
        createBoard(HARD_V, "red");
        lblTime.setText(HARD_V + "");
        setPlayer();
    }

    private void createRooms(int rooms, int type) {
        mController.getMaze().addRoom(0, 1);
        for (int i = 1; i < rooms - 1; i++) {
            mController.getMaze().addRoom(i, type);
        }
        mController.getMaze().addRoom(rooms - 1, 1);
        mController.getMaze().setTreasure(rooms - 1);
        lblRooms.setText(rooms + "");
    }

    private void createDoors(int render, int type) {
        Maze m = mController.getMaze();
        for (int i = 0; i < render * render; i++) {
            if ((i + 1) % render != 0) {
                m.addDoor(m.getGraph().getVertex(i).getData(), m.getGraph().getVertex(i + 1).getData(), type);
            }
        }
        for (int i = 0; i < render * render; i++) {
            if ((double) ((i + 1.0) / render) <= (render - 1)) {
                m.addDoor(m.getGraph().getVertex(i).getData(), m.getGraph().getVertex(i + render).getData(), type);
            }
        }
    }

    private void createBoard(int amount, String color) {
        Maze m = mController.getMaze();
        board.getChildren().clear();
        int rowsAndCols = (int) Math.sqrt(amount);
        int idRoom = 0;
        for (int i = 0; i < rowsAndCols; i++) {
            for (int j = 0; j < rowsAndCols; j++) {
                GridPane tempGrid = new GridPane();

                Label lblId = new Label(idRoom + "");
                ImageView img = roomImage(m.getGraph().getVertex(idRoom).getData());

                tempGrid.add(lblId, 0, 0);
                tempGrid.add(img, 3, 0);

                for (int k = 0; k < m.getGraph().getVertex(idRoom).getEdges().size(); k++) {
                    Door current = m.getGraph().getVertex(idRoom).getEdges().get(k).getData();
                    Room s = m.getGraph().getVertex(idRoom).getEdges().get(k).getSource().getData();
                    Room d = m.getGraph().getVertex(idRoom).getEdges().get(k).getDestination().getData();

                    int source = (s.getId() == idRoom) ? s.getId() : d.getId();
                    int destination = (d.getId() == idRoom) ? s.getId() : d.getId();
                    int[] pos = getPosition(source, destination, rowsAndCols);

                    ImageView dImg = doorImage(current);
                    Label dToken = new Label(current.getToken() + "");

                    tempGrid.add(dImg, pos[0], pos[1]);
                    tempGrid.add(dToken, pos[2], pos[3]);
                }
                completeGridNodes(tempGrid);
                tempGrid.setStyle(
                        "-fx-border-color:" + color + "; -fx-border-radius: 20px; -fx-background-radius: 20px;");
                boxStyle(tempGrid, 0, tempGrid.getChildren().size());
                board.add(tempGrid, j, i);
                idRoom++;
            }
        }
        boxStyle(board, 0, board.getChildren().size());
        board.setHgap(10);
        board.setVgap(10);
        board.setPadding(new Insets(10, 10, 10, 10));
        board.requestFocus();

        player.setTokens(mController.getMaze().minimumPath(0));
        lblTokens.setText(player.getTokens() + "");
    }

    private Stage loadModal(Route route) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(route.getRoute()));
        fxmlLoader.setController(this);
        Stage stage = new Stage();
        try {
            Parent modal = fxmlLoader.load();
            Scene scene = new Scene(modal);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
        stage.initStyle(StageStyle.TRANSPARENT);
        return stage;
    }

    @FXML
    public void finish(ActionEvent event) {
        modal.close();
        mController.renderScreen(Route.WELCOME);
    }

    private int[] getPosition(int s, int d, int props) {
        int[] pos = new int[4];
        if (s - d == 1) {
            pos[0] = 0;
            pos[1] = 1;
            pos[2] = 0;
            pos[3] = 2;
        } else if (s - d == -1) {
            pos[0] = 3;
            pos[1] = 1;
            pos[2] = 3;
            pos[3] = 2;
        } else if (s - d == props) {
            pos[0] = 1;
            pos[1] = 0;
            pos[2] = 2;
            pos[3] = 0;
        } else {
            pos[0] = 1;
            pos[1] = 3;
            pos[2] = 2;
            pos[3] = 3;
        }
        return pos;
    }

    private void completeGridNodes(GridPane gPane) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (getNodeByRowColumnIndex(i, j, gPane) == null) {
                    gPane.add(new Text(), j, i);
                }
            }
        }
    }

    private void renderImg(ImageView img) {
        img.setFitHeight(20);
        img.setFitWidth(20);
    }

    private ImageView roomImage(Room r) {
        ImageView img = null;
        if (r instanceof KeyRoom) {
            img = new ImageView(new Image(Route.KEY.getRoute()));
        } else if (r instanceof EnchantedRoom) {
            img = new ImageView(new Image(Route.ENCHANTED.getRoute()));
        } else {
            TraditionalRoom tRoom = (TraditionalRoom) r;
            if (tRoom.hasTreasure()) {
                img = new ImageView(new Image(Route.TREASURE.getRoute()));
            } else {
                img = new ImageView(new Image(Route.TRADITIONAL.getRoute()));
            }
        }
        renderImg(img);
        return img;
    }

    private ImageView doorImage(Door d) {
        ImageView img = null;
        if (d instanceof SilverDoor) {
            img = new ImageView(new Image(Route.SILVER.getRoute()));
        } else if (d instanceof GoldenDoor) {
            img = new ImageView(new Image(Route.GOLD.getRoute()));
        } else {
            img = new ImageView(new Image(Route.INFERNAL.getRoute()));
        }
        renderImg(img);
        return img;
    }

    private void boxStyle(GridPane gP, int n, int i) {
        if (n < i) {
            GridPane.setFillHeight(gP.getChildren().get(n), true);
            GridPane.setFillWidth(gP.getChildren().get(n), true);
            GridPane.setHgrow(gP.getChildren().get(n), Priority.ALWAYS);
            GridPane.setVgrow(gP.getChildren().get(n), Priority.ALWAYS);
            GridPane.setHalignment(gP.getChildren().get(n), HPos.CENTER);
            GridPane.setValignment(gP.getChildren().get(n), VPos.CENTER);
            n++;
            boxStyle(gP, n, i);
        }
    }

    @FXML
    public void movement(KeyEvent event) {
        int recentId = player.getIdRoom();
        int cost = 0;
        GridPane temp = (GridPane) board.getChildren().get(recentId);
        switch (event.getCode()) {
            case UP:
                if (getNodeByRowColumnIndex(0, 2, temp) != null) {
                    player.setIdRoom(recentId - endPoint);
                    temp.getChildren().remove(getNodeByRowColumnIndex(1, 1, temp));
                    Label n = (Label) getNodeByRowColumnIndex(0, 2, temp);
                    cost = Integer.parseInt(n.getText());
                    setTokens(cost);
                }
                break;
            case DOWN:
                if (getNodeByRowColumnIndex(3, 2, temp) != null) {
                    player.setIdRoom(recentId + endPoint);
                    temp.getChildren().remove(getNodeByRowColumnIndex(1, 1, temp));
                    Label n = (Label) getNodeByRowColumnIndex(3, 2, temp);
                    cost = Integer.parseInt(n.getText());
                    setTokens(cost);
                }
                break;
            case RIGHT:
                if (getNodeByRowColumnIndex(2, 3, temp) != null) {
                    player.setIdRoom(recentId + 1);
                    temp.getChildren().remove(getNodeByRowColumnIndex(1, 1, temp));
                    Label n = (Label) getNodeByRowColumnIndex(2, 3, temp);
                    cost = Integer.parseInt(n.getText());
                    setTokens(cost);
                }
                break;
            case LEFT:
                if (getNodeByRowColumnIndex(2, 0, temp) != null) {
                    player.setIdRoom(recentId - 1);
                    temp.getChildren().remove(getNodeByRowColumnIndex(1, 1, temp));
                    Label n = (Label) getNodeByRowColumnIndex(2, 0, temp);
                    cost = Integer.parseInt(n.getText());
                    setTokens(cost);
                }
                break;
            default:
                break;
        }
    }

    private Node getNodeByRowColumnIndex(final int row, final int column, GridPane gridPane) {
        Node result = null;
        ObservableList<Node> childrens = gridPane.getChildren();
        for (Node node : childrens) {
            if (!(node instanceof Text) && GridPane.getRowIndex(node) == row
                    && GridPane.getColumnIndex(node) == column) {
                result = node;
                break;
            }
        }
        return result;
    }

    private void setPlayer() {
        ImageView avatar = new ImageView(new Image(Route.AVATAR.getRoute() + player.getAvatar() + ".png"));
        avatar.setFitHeight(40);
        avatar.setFitWidth(40);
        GridPane temp = (GridPane) board.getChildren().get(player.getIdRoom());
        temp.add(avatar, 1, 1);
        GridPane.setConstraints(avatar, 1, 1, 2, 2, HPos.CENTER, VPos.CENTER);
    }

    private void hasClue() {
        Room room = mController.getMaze().getGraph().getVertex(player.getIdRoom()).getData();
        if (room instanceof EnchantedRoom) {
            int clue = mController.getMaze().minimumPathBetweenPairs(player.getIdRoom());
            EnchantedRoom eRoom = (EnchantedRoom) room;
            eRoom.setClue(clue);
            GridPane tempGrid = (GridPane) board.getChildren().get(clue);
            tempGrid.setStyle(
                    "-fx-border-color: purple; -fx-border-radius: 20px; -fx-background-radius: 20px;-fx-background-color: yellow");
        }
    }

    private void hasKey() {
        if (mController.getMaze().getGraph().getVertex(player.getIdRoom()).getData() instanceof KeyRoom) {
            key.setVisible(true);
            player.setHasKey(true);
        }
    }

    private void loadStage() {
        if (modal == null) {
            modal = loadModal(Route.MODAL);
            modal.show();
        }
    }

    public synchronized void isWinner() {
        if (player.getTokens() < 0 || lblTime.getText().equals("0")) {
            player.setWinner(true);
            loadStage();
            lblPlayerState.setText("!Loser!");
            path.setText(mController.getMaze().getPath());
        } else if (player.getIdRoom() == board.getChildren().size() - 1) {
            player.setWinner(true);
            loadStage();
            lblPlayerState.setText((player.isHasKey()) ? "!Winner with Key!" : "!Winner!");
            path.setText(mController.getMaze().getPath());
        }
    }

    private void setTokens(int amountToMoves) {
        player.decreaseTokens(amountToMoves);
        hasKey();
        hasClue();
        isWinner();
        lblTokens.setText(player.getTokens() + "");
        setPlayer();
    }

}
