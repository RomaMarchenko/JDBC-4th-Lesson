package lesson4HW;

import exceptions.BadRequestException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Controller {
    private static final String DB_URL = "jdbc:oracle:thin:@gromcode-lessons.ceffzvpakwhb.us-east-2.rds.amazonaws.com:1521:ORCL";

    private static final String USER = "main";
    private static final String PASS = "main2001";

    private static final String PUT_FILE_TO_STORAGE = "UPDATE FILES SET STORAGE = ? WHERE FILE_ID = ?";
    private static final String GET_FILES_ID_FROM_STORAGE_BY_ID = "SELECT FILE_ID FROM FILES WHERE STORAGE = ?";
    private static final String DELETE_FILE_FROM_STORAGE_BY_ID = "DELETE FROM FILES WHERE STORAGE = ? AND FILE_ID = ?";
    private static final String TRANSFER_FILE = "UPDATE FILES SET STORAGE = ? WHERE STORAGE = ? AND FILE_ID = ?";
    private static final String GET_ALL_FILES_FROM_STORAGE = "SELECT * FROM FILES WHERE STORAGE = ?";
    private static final String GET_STORAGE_BY_ID = "SELECT * FROM STORAGE WHERE ID = ?";
    private static final String GET_FILE_BY_ID = "SELECT * FROM FILES WHERE FILE_ID = ?";

    public static void put(Storage storage, File file) throws BadRequestException {
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(PUT_FILE_TO_STORAGE);
            if (storage != null && file != null) {
                checkFile(file, storage);
                checkStorage(storage, file.getId());

                preparedStatement.setLong(1, storage.getId());
                preparedStatement.setLong(2, file.getId());

                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Something went wrong");
            e.printStackTrace();
        }
    }

    public static void putAll(Storage storage, List<File> files) {
        try (Connection connection = getConnection()) {
            putFilesList(storage, files, connection);
        } catch (SQLException | BadRequestException e) {
            System.err.println("Something went wrong");
            e.printStackTrace();
        }
    }

    public static void delete(Storage storage, File file) {
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(DELETE_FILE_FROM_STORAGE_BY_ID);
            preparedStatement.setLong(1, storage.getId());
            preparedStatement.setLong(2, file.getId());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Something went wrong");
            e.printStackTrace();
        }
    }

    public static void transferAll(Storage storageFrom, Storage storageTo) {
        try (Connection connection = getConnection()) {
            transferAllFiles(storageFrom, storageTo, connection);
        } catch (SQLException e) {
            System.err.println("Something went wrong");
            e.printStackTrace();
        }
    }

    public static void transferFile(Storage storageFrom, Storage storageTo, long id) {
        try(Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(TRANSFER_FILE);
            validateFileTransferBetweenStorages(getFileById(id), storageTo);

            preparedStatement.setLong(1, storageTo.getId());
            preparedStatement.setLong(2, storageFrom.getId());
            preparedStatement.setLong(3, id);

            preparedStatement.executeUpdate();
        } catch (SQLException | BadRequestException e) {
            System.err.println("Something went wrong");
            e.printStackTrace();
        }
    }

    public static File getFileById(long id) throws SQLException {
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(GET_FILE_BY_ID);
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            return mapFile(resultSet);
        }
    }

    public static Storage getStorageById(long storageId) throws SQLException {
        try(Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(GET_STORAGE_BY_ID);
            preparedStatement.setLong(1, storageId);

            ResultSet resultSet = preparedStatement.executeQuery();

            return mapStorage(resultSet);
        }
    }

    private static void putFilesList(Storage storage, List<File> files, Connection connection) throws SQLException, BadRequestException {
        int fileNum = 0;
        try (PreparedStatement preparedStatement = connection.prepareStatement(PUT_FILE_TO_STORAGE)) {
            connection.setAutoCommit(false);
            for (File file : files) {
                if (storage != null && file != null) {
                    checkFile(file, storage);
                    checkStorage(storage, file.getId());

                    preparedStatement.setLong(1, storage.getId());
                    preparedStatement.setLong(2, file.getId());

                    preparedStatement.executeUpdate();
                    }
                }
            connection.commit();
            } catch(SQLException | BadRequestException e) {
                connection.rollback();
                System.err.println("Transaction was failed to perform on file with ID: " + files.get(fileNum).getId());
                throw e;
            }
        }

    private static void transferAllFiles(Storage storageFrom, Storage storageTo, Connection connection) throws SQLException {
        ArrayList<File> files = (ArrayList<File>) getAllFilesFromStorage(storageFrom.getId());
        int fileNum = 0;
        try (PreparedStatement preparedStatement = connection.prepareStatement(TRANSFER_FILE)) {
            connection.setAutoCommit(false);

            for (File file : files) {
                validateFileTransferBetweenStorages(file, storageTo);

                preparedStatement.setLong(1, storageTo.getId());
                preparedStatement.setLong(2, storageFrom.getId());
                preparedStatement.setLong(3, file.getId());

                preparedStatement.executeUpdate();
            }
            connection.commit();
        } catch (BadRequestException | SQLException e) {
            connection.rollback();
            System.err.println("Transaction was failed to perform on product with ID: " + files.get(fileNum).getId());
            e.printStackTrace();
        }
    }

    private static void checkFile(File file, Storage storage) throws BadRequestException, SQLException {
        if (file.getId() == null)
            throw new IllegalArgumentException("File id cannot be null");

        if (file.getStorage() != null)
            throw new BadRequestException("File with ID: " + file.getId() + " is already in storage. You can't add one file to more than one storage");

        String[] supportedFormats = storage.getFormatsSupported().split(", ");
        for (int index = 0; index <= supportedFormats.length; index++) {
            if(index == supportedFormats.length)
                throw new BadRequestException("Storage with ID: " + storage.getId() + " doesn't support " + file.getFormat() + " format. File with ID: " + file.getId() + " is not added to storage");
            if(supportedFormats[index].equals(file.getFormat()))
                break;
        }

        for (long id : getFilesIdFromStorage(storage.getId())) {
            if(id == file.getId())
                throw new BadRequestException("Storage with ID: " + storage.getId() + " already contains file with ID: " + file.getId() + ". File is not added to storage");
        }
    }

    private static void checkStorage(Storage storage, long fileId) throws SQLException, BadRequestException {
        if (storage.getId() == null)
            throw new IllegalArgumentException("Storage id cannot be null");
        if (storage.getStorageMaxSize() <= getFilesIdFromStorage(storage.getId()).size()) {
            throw new BadRequestException("Storage with ID: " + storage.getId() + " has not empty space. File with ID: " + fileId + " is not added to storage");
        }
    }

    private static void validateFileTransferBetweenStorages(File file, Storage storage) throws BadRequestException, SQLException {
        if (file != null && storage != null) {
            if (file.getId() == null || storage.getId() == null)
                throw new IllegalArgumentException("Both storage and file ID cannot be null");

            String[] supportedFormats = storage.getFormatsSupported().split(", ");
            for (int index = 0; index <= supportedFormats.length; index++) {
                if(index == supportedFormats.length)
                    throw new BadRequestException("Storage with ID: " + storage.getId() + " doesn't support " + file.getFormat() + " format. File with ID: " + file.getId() + " is not added to storage");
                if(supportedFormats[index].equals(file.getFormat()))
                    break;
            }

            for (long id : getFilesIdFromStorage(storage.getId())) {
                if(id == file.getId())
                    throw new BadRequestException("Storage with ID: " + storage.getId() + " already contains file with ID: " + file.getId() + ". File is not added to storage");
            }

            if (storage.getStorageMaxSize() <= getFilesIdFromStorage(storage.getId()).size())
                throw new BadRequestException("Storage with ID: " + storage.getId() + " has not empty space. File with ID: " + file.getId() + " is not added to storage");
        }
    }

    public static List<Long> getFilesIdFromStorage(long storageId) throws SQLException {
        try(Connection connection = getConnection()) {
            List<Long> filesId = new ArrayList<>();

            PreparedStatement preparedStatement = connection.prepareStatement(GET_FILES_ID_FROM_STORAGE_BY_ID);
            preparedStatement.setLong(1, storageId);

            ResultSet resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                filesId.add(resultSet.getLong(1));
            }

            return filesId;
        }
    }

    public static List<File> getAllFilesFromStorage(long storageId) throws  SQLException {
        try(Connection connection = getConnection()) {
            List<File> files = new ArrayList<>();

            PreparedStatement preparedStatement = connection.prepareStatement(GET_ALL_FILES_FROM_STORAGE);
            preparedStatement.setLong(1, storageId);

            ResultSet resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                files.add(mapFile(resultSet));
            }

            return files;
        }
    }

    private static File mapFile(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            return new File(resultSet.getLong("FILE_ID"), resultSet.getString("FILE_NAME"), resultSet.getString("FILE_FORMAT"), resultSet.getLong("FILE_SIZE"), getStorageById(resultSet.getLong("STORAGE")));
        }
        return null;
    }


    private static Storage mapStorage(ResultSet resultSet) throws SQLException {
        if (resultSet.next())
            return new Storage(resultSet.getLong("ID"), resultSet.getString("FORMATS_SUPPORTED").split(", "), resultSet.getString("STORAGE_COUNTRY"), resultSet.getLong("STORAGE_MAX_SIZE"));
        return null;
    }



    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }
}