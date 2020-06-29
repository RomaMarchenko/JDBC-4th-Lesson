package lesson4HW;

import java.util.ArrayList;
import java.util.List;

public class Demo {
    public static void main(String[] args) throws Exception{
        Storage storage1 = Controller.getStorageById(1000);
        Storage storage2 = Controller.getStorageById(1001);
        Storage storage3 = Controller.getStorageById(1002);

        File file1 = Controller.getFileById(100);
        File file2 = Controller.getFileById(101);
        File file3 = Controller.getFileById(102);
        File file4 = Controller.getFileById(103);
        File file5 = Controller.getFileById(104);
        File file6 = Controller.getFileById(105);
        File file7 = Controller.getFileById(106);
        File file8 = Controller.getFileById(107);
        File file9 = Controller.getFileById(108);
        File file10 = Controller.getFileById(109);
        File file11 = Controller.getFileById(110);

        List<File> files1 = new ArrayList<>();

        files1.add(file3);

        List<File> files2 = new ArrayList<>();
        files2.add(file5);
        files2.add(file10);

        List<File> files3 = new ArrayList<>();

        files3.add(file7);
        files3.add(file6);

        //Controller.put(storage1, file1);
        //Controller.put(storage1, file10);
        //Controller.put(storage1, file8);

        //Controller.putAll(storage1, files1);
        //Controller.putAll(storage1, files2);
        //Controller.putAll(storage1, files3);

        //Controller.delete(storage1, file2);
        //Controller.delete(storage1, file7);

        //Controller.transferAll(storage1, storage2);
        //Controller.transferAll(storage1, storage3);

        Controller.transferFile(storage3, storage1, 100);
        Controller.transferFile(storage3, storage1, 105);
    }
}
