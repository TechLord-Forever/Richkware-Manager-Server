package richk.RMS.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import richk.RMS.model.Device;
import richk.RMS.model.Model;
import richk.RMS.model.ModelException;

public class DatabaseManager implements Model{
	private String dbUrl;
	private String dbUsername;
	private String dbPassword;
	
	public DatabaseManager() throws DatabaseException{
		ResourceBundle resource = ResourceBundle.getBundle("configuration");
		dbUsername = resource.getString("database.username");
		dbPassword = resource.getString("database.password");

		dbUrl = resource.getString("database.url");
		String dbClass = resource.getString("database.class");
		try {
			Class.forName(dbClass);
		} catch (ClassNotFoundException e) {
			throw new DatabaseException(e);
		}

	}

	private Connection connect() throws DatabaseException{
		try {
			return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	private void disconnect(Connection connection, PreparedStatement preparedStatement, ResultSet resultSet) throws DatabaseException{
		try {
			resultSet.close();
		}catch(SQLException e){
			throw new DatabaseException(e);
		}catch (Exception e1) {		
		}
		try {
			preparedStatement.close();
		} catch (Exception e) {
			throw new DatabaseException(e);
		}
		try {
			connection.close();
		} catch (Exception e) {
			throw new DatabaseException(e);
		}

	}

	@Override
	public List<Device> RefreshDevice() throws ModelException {
		List<Device> deviceList = new ArrayList<>();

		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try{
			connection = connect();
			preparedStatement = connection.prepareStatement("SELECT * FROM Device");
			resultSet = preparedStatement.executeQuery();

			while(resultSet.next()){
				Device tmp = new Device(resultSet.getString("Name"),resultSet.getString("IP"),resultSet.getString("ServerPort"),resultSet.getString("LastConnection"));
				deviceList.add(tmp);
			}
		}
		catch(SQLException e){
			disconnect(connection, preparedStatement, resultSet);
		}
		disconnect(connection, preparedStatement, resultSet);
		return deviceList;
	}

	@Override
	public boolean AddDevice(Device device) throws ModelException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
	
		try{
			connection = connect();
			preparedStatement = connection.prepareStatement	("INSERT INTO Device (NAME, IP, SERVERPORT, LASTCONNECTION) VALUES (?,?,?,?)");
			preparedStatement.setString(1, device.getName());
			preparedStatement.setString(2, device.getIP());
			preparedStatement.setString(3, device.getServerPort());
			preparedStatement.setString(4, device.getLastConnection());
			preparedStatement.executeUpdate();
		}
		catch(SQLException e){
			disconnect(connection, preparedStatement, resultSet);
			return false;
		}
		disconnect(connection, preparedStatement, resultSet);
		return true;
	}


	@Override
	public boolean RemoveDevice(String name) throws ModelException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try{
			connection = connect();
			preparedStatement = connection.prepareStatement	("DELETE FROM Device WHERE Name = ?");
			preparedStatement.setString(1, name);
			preparedStatement.executeUpdate();
		}
		catch(SQLException e){
			disconnect(connection, preparedStatement, resultSet);
			return false;
		}
		disconnect(connection, preparedStatement, resultSet);
		return true;
	}
}