package application;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javafx.application.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.event.*;

/**
 * This class create a simple GUI with a set of button, a set of text field and a text area where the HTTP response are represented
 * On a button click, the corresponding action are performed: each action performs a specific http request.
**/
public class GUI extends Application {

    private Label la;
    private TextField add_vlan_tf, delete_vlan_tf, add_host_to_vlan_tf_vlanid, add_host_to_vlan_tf_hostid, remove_host_from_vlan_tf, view_vlan_hosts_tf;
    private TextArea ta;
    private Button add_vlan_button, delete_vlan_button, view_vlans_button, add_host_to_vlan_button, remove_host_from_vlan_button, view_vlan_hosts_button, clear_button;

    private static double SCENE_WIDTH;
    private static double SCENE_HEIGHT;

    private static double TEXT_FIELD_WIDTH;
    private static double TEXT_FIELD_HEIGHT;
    private static int TEXT_AREA_WIDTH; //Express in # of column
    private static int TEXT_AREA_HEIGHT; //Express in # of row

    private static double LABEL_X_LAYOUT;
    private static double BUTTON_X_LAYOUT;
    private static double BUTTON_Y_LAYOUT;
    private static double BUTTON_INTERSPACING;

    private static double TEXT_FIELD_X_LAYOUT;
    private static double TEXT_FIELD_Y_LAYOUT;

    private static double TEXT_AREA_X_LAYOUT;
    private static double TEXT_AREA_Y_LAYOUT;

    private final static int TEXT_AREA_FONT_SIZE = 20;
    private final static String LABEL_STYLE = "-fx-font-size: 40px; -fx-text-fill: blue; -fx-font-weight: bold;";
    private final static String BUTTON_STYLE = "-fx-font-size: 20px; -fx-text-fill: black; -fx-font-weight: bold;";
    private final static String TEXT_FIELD_STYLE = "-fx-font-size: 20px; -fx-text-fill: black;";
    private final static String TEXT_AREA_STYLE = "-fx-font-size: " + TEXT_AREA_FONT_SIZE + "px; -fx-text-fill: black;";

    public void start(Stage stage) {

        la = new Label("VLAN Management System");
        ta = new TextArea("");

        setDimension();

        add_vlan_tf = new TextField("");
        delete_vlan_tf = new TextField("");
        add_host_to_vlan_tf_vlanid = new TextField("");
        add_host_to_vlan_tf_hostid = new TextField("");
        remove_host_from_vlan_tf = new TextField("");
        view_vlan_hosts_tf = new TextField("");

        add_vlan_button = new Button("Add VLAN");
        delete_vlan_button = new Button("Delete VLAN");
        view_vlans_button = new Button("View VLANs");
        add_host_to_vlan_button = new Button("Add Host to VLAN");
        remove_host_from_vlan_button = new Button("Remove Host from VLAN");
        view_vlan_hosts_button = new Button("View VLAN Hosts");
        clear_button = new Button("Clear");

        //Label
        la.setLayoutX( LABEL_X_LAYOUT );
        la.setStyle( LABEL_STYLE );

        //Text Area
        ta.setStyle( TEXT_AREA_STYLE );
        ta.setLayoutX( TEXT_AREA_X_LAYOUT ); 	ta.setLayoutY( TEXT_AREA_Y_LAYOUT );
        ta.setPrefColumnCount( TEXT_AREA_WIDTH ); 	ta.setPrefRowCount( TEXT_AREA_HEIGHT );

        //Buttons
        add_vlan_button.setStyle( BUTTON_STYLE );
        add_vlan_button.setLayoutX( BUTTON_X_LAYOUT ); add_vlan_button.setLayoutY( BUTTON_Y_LAYOUT + BUTTON_INTERSPACING );

        delete_vlan_button.setStyle( BUTTON_STYLE );
        delete_vlan_button.setLayoutX( BUTTON_X_LAYOUT ); delete_vlan_button.setLayoutY( add_vlan_button.getLayoutY() + BUTTON_INTERSPACING );

        view_vlans_button.setStyle( BUTTON_STYLE );
        view_vlans_button.setLayoutX( BUTTON_X_LAYOUT ); view_vlans_button.setLayoutY( delete_vlan_button.getLayoutY() + BUTTON_INTERSPACING );

        add_host_to_vlan_button.setStyle( BUTTON_STYLE );
        add_host_to_vlan_button.setLayoutX( BUTTON_X_LAYOUT ); add_host_to_vlan_button.setLayoutY( view_vlans_button.getLayoutY() + BUTTON_INTERSPACING );

        remove_host_from_vlan_button.setStyle( BUTTON_STYLE );
        remove_host_from_vlan_button.setLayoutX( BUTTON_X_LAYOUT ); remove_host_from_vlan_button.setLayoutY( add_host_to_vlan_button.getLayoutY() + BUTTON_INTERSPACING );

        view_vlan_hosts_button.setStyle( BUTTON_STYLE );
        view_vlan_hosts_button.setLayoutX( BUTTON_X_LAYOUT ); view_vlan_hosts_button.setLayoutY( remove_host_from_vlan_button.getLayoutY() + BUTTON_INTERSPACING );

        clear_button.setStyle( BUTTON_STYLE );
        clear_button.setLayoutX( TEXT_AREA_X_LAYOUT ); clear_button.setLayoutY( ta.getLayoutY() + (ta.getPrefRowCount() +3) *TEXT_AREA_FONT_SIZE + BUTTON_INTERSPACING );

        //Text Fields
        add_vlan_tf.setStyle( TEXT_FIELD_STYLE );
        add_vlan_tf.setPromptText("vlan");
        add_vlan_tf.setLayoutX( TEXT_FIELD_X_LAYOUT); 	add_vlan_tf.setLayoutY( add_vlan_button.getLayoutY() );
        add_vlan_tf.setMaxWidth( TEXT_FIELD_WIDTH );	add_vlan_tf.setMaxHeight( TEXT_FIELD_HEIGHT );

        delete_vlan_tf.setStyle( TEXT_FIELD_STYLE );
        delete_vlan_tf.setPromptText("vlan");
        delete_vlan_tf.setLayoutX( TEXT_FIELD_X_LAYOUT ); 	delete_vlan_tf.setLayoutY( delete_vlan_button.getLayoutY() );
        delete_vlan_tf.setMaxWidth( TEXT_FIELD_WIDTH );	delete_vlan_tf.setMaxHeight( TEXT_FIELD_HEIGHT );

        add_host_to_vlan_tf_vlanid.setStyle( TEXT_FIELD_STYLE );
        add_host_to_vlan_tf_vlanid.setPromptText("vlan");
        add_host_to_vlan_tf_vlanid.setLayoutX( TEXT_FIELD_X_LAYOUT); 	add_host_to_vlan_tf_vlanid.setLayoutY( add_host_to_vlan_button.getLayoutY() );
        add_host_to_vlan_tf_vlanid.setMaxWidth( TEXT_FIELD_WIDTH );	add_host_to_vlan_tf_vlanid.setMaxHeight( TEXT_FIELD_HEIGHT );

        add_host_to_vlan_tf_hostid.setStyle( TEXT_FIELD_STYLE );
        add_host_to_vlan_tf_hostid.setPromptText("host");
        add_host_to_vlan_tf_hostid.setLayoutX( TEXT_FIELD_X_LAYOUT + add_host_to_vlan_tf_vlanid.getMaxWidth() + 30 ); 	add_host_to_vlan_tf_hostid.setLayoutY( add_host_to_vlan_button.getLayoutY() );
        add_host_to_vlan_tf_hostid.setMaxWidth( TEXT_FIELD_WIDTH );	add_host_to_vlan_tf_hostid.setMaxHeight( TEXT_FIELD_HEIGHT );

        remove_host_from_vlan_tf.setStyle( TEXT_FIELD_STYLE );
        remove_host_from_vlan_tf.setPromptText("host");
        remove_host_from_vlan_tf.setLayoutX( TEXT_FIELD_X_LAYOUT ); 	remove_host_from_vlan_tf.setLayoutY( remove_host_from_vlan_button.getLayoutY() );
        remove_host_from_vlan_tf.setMaxWidth( TEXT_FIELD_WIDTH );	remove_host_from_vlan_tf.setMaxHeight( TEXT_FIELD_HEIGHT );

        view_vlan_hosts_tf.setStyle( TEXT_FIELD_STYLE );
        view_vlan_hosts_tf.setPromptText("vlan");
        view_vlan_hosts_tf.setLayoutX( TEXT_FIELD_X_LAYOUT ); 	view_vlan_hosts_tf.setLayoutY( view_vlan_hosts_button.getLayoutY() );
        view_vlan_hosts_tf.setMaxWidth( TEXT_FIELD_WIDTH );	view_vlan_hosts_tf.setMaxHeight( TEXT_FIELD_HEIGHT );

        //Define action on click
        add_vlan_button.setOnAction( (ActionEvent ev)->{ addVLAN(); });
        delete_vlan_button.setOnAction( (ActionEvent ev)->{ deleteVLAN(); });
        view_vlans_button.setOnAction( (ActionEvent ev)->{ viewVLANS(); });
        add_host_to_vlan_button.setOnAction( (ActionEvent ev)->{ addHostToVLAN(); });
        remove_host_from_vlan_button.setOnAction( (ActionEvent ev)->{ removeHostFromVLAN(); });
        view_vlan_hosts_button.setOnAction( (ActionEvent ev)->{ viewVLANHosts(); });
        clear_button.setOnAction( (ActionEvent ev)->{ clear(); });

       // stage.setOnCloseRequest((WindowEvent we) -> { method();} );

        Group root = new Group(la, ta, add_vlan_tf, delete_vlan_tf, add_host_to_vlan_tf_vlanid, add_host_to_vlan_tf_hostid,
        		remove_host_from_vlan_tf, view_vlan_hosts_tf, add_vlan_button, delete_vlan_button, view_vlans_button, add_host_to_vlan_button,
        		remove_host_from_vlan_button, view_vlan_hosts_button, clear_button );
        Scene scene = new Scene( root, SCENE_WIDTH, SCENE_HEIGHT );

        stage.setTitle("VLAN Management System");
        stage.setScene( scene );
        stage.show();

    }
    
	public static void main(String[] args) {
		launch(args);
	}

/************************************************** END STAGE *****************************************************/
/************************************************** ACTION IMPLEMENTATION ******************************************/
	/**
	 * Each method create a HTTP Manager with a predefined string, and after that invoke the doHTTPRequest method.
	 * The http response is a string and is shown in the text area.
	**/
    private void addVLAN() {
    	ta.setText( "ADD VLAN BUTTON PRESSED" );
    	HTTPManager hm = new HTTPManager( "http://127.0.0.1:8080/vlanmanagement/addvlan/json");
    	Map<String, String> parameters = new HashMap<>();
    	parameters.put( "vlanid", add_vlan_tf.getText());
    	
    	String resp = hm.doHTTPRequest( hm.POST_REQUEST, parameters);

    	ta.appendText("\n" + resp );
    }

    private void deleteVLAN() {
    	ta.setText( "DELETE VLAN BUTTON PRESSED" );
    	HTTPManager hm = new HTTPManager( "http://127.0.0.1:8080/vlanmanagement/deletevlan/json");
    	Map<String, String> parameters = new HashMap<>();
    	parameters.put("vlanid", delete_vlan_tf.getText());
    	
    	String resp = hm.doHTTPRequest( hm.POST_REQUEST, parameters);

    	ta.appendText("\n" + resp );

    }

    private void viewVLANS() {
    	ta.setText( "VIEW VLANS BUTTON PRESSED" );
    	HTTPManager hm = new HTTPManager( "http://127.0.0.1:8080/vlanmanagement/showvlans/json");
    	String resp = hm.doHTTPRequest( hm.GET_REQUEST, null );

    	ta.appendText("\n" + resp );
    }

    private void addHostToVLAN() {
    	ta.setText( "ADD HOST TO VLAN BUTTON PRESSED" );
    	HTTPManager hm = new HTTPManager( "http://127.0.0.1:8080/vlanmanagement/addhosttovlan/json");
    	Map<String, String> parameters = new HashMap<>();
    	parameters.put("vlanid", add_host_to_vlan_tf_vlanid.getText());
    	parameters.put("hostid", add_host_to_vlan_tf_hostid.getText());
    	
    	String resp = hm.doHTTPRequest( hm.POST_REQUEST, parameters);

    	ta.appendText("\n" + resp );
    }

    private void removeHostFromVLAN() {
    	ta.setText( "REMOVE HOST FROM VLAN BUTTON PRESSED" );
    	HTTPManager hm = new HTTPManager( "http://127.0.0.1:8080/vlanmanagement/removehostfromvlan/json");
    	Map<String, String> parameters = new HashMap<>();
    	parameters.put("hostid", remove_host_from_vlan_tf.getText());
    	
    	String resp = hm.doHTTPRequest( hm.POST_REQUEST, parameters );

    	ta.appendText("\n" + resp );
 
    }

    private void viewVLANHosts()  {
    	ta.setText( "VIEW VLAN HOSTS BUTTON PRESSED" );
    	HTTPManager hm = new HTTPManager( "http://127.0.0.1:8080/vlanmanagement/showvlanhosts/json");
    	Map<String, String> parameters = new HashMap<>();
    	parameters.put("vlanid", view_vlan_hosts_tf.getText());
    	
    	String resp = hm.doHTTPRequest( hm.POST_REQUEST, parameters);
    	 
    	ta.appendText("\n" + resp );
    }

	//Clean completely the text area 
    private void clear()  {
    	ta.clear();
    }

	/**
	 * First of all, this method recovers the dimension of the screen, and after that set all the object dimension.
	**/
    private void setDimension() {
    	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    	SCENE_WIDTH = screenSize.getWidth() * 0.9;
    	SCENE_HEIGHT = screenSize.getHeight() * 0.9;

        TEXT_AREA_WIDTH = (int)(0.02*SCENE_WIDTH);//Express in # of column
        TEXT_AREA_HEIGHT = (int)(0.03*SCENE_HEIGHT);//Express in # of row
        
        TEXT_FIELD_WIDTH = 0.1*SCENE_WIDTH;
        TEXT_FIELD_HEIGHT = 0.5*SCENE_HEIGHT;

    	LABEL_X_LAYOUT = 0.3*SCENE_WIDTH;
        BUTTON_X_LAYOUT = 0.01*SCENE_WIDTH;
        BUTTON_Y_LAYOUT = 0.02*SCENE_HEIGHT;
        BUTTON_INTERSPACING = 0.1*SCENE_HEIGHT;

        TEXT_FIELD_X_LAYOUT = 0.3*SCENE_WIDTH;
        TEXT_FIELD_Y_LAYOUT = BUTTON_Y_LAYOUT;

        TEXT_AREA_X_LAYOUT = 0.55*SCENE_WIDTH;
        TEXT_AREA_Y_LAYOUT = 0.1*SCENE_HEIGHT;
    }
    
}