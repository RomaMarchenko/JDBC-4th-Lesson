package lesson4HW;

import java.util.Arrays;

public class Storage {
    private Long id;
    private String[] formatsSupported;
    private String storageCountry;
    private Long storageMaxSize;

    public Storage(Long id, String[] formatsSupported, String storageCountry, Long storageMaxSize) {
        this.id = id;
        this.formatsSupported = formatsSupported;
        checkFormats(formatsSupported, id);
        this.storageCountry = storageCountry;
        this.storageMaxSize = storageMaxSize;
    }


    public Long getId() {
        return id;
    }

    public String getFormatsSupported() {
        String formats = Arrays.toString(formatsSupported);
        return formats.substring(1, formats.length() - 1);
    }

    public String getStorageCountry() {
        return storageCountry;
    }

    public Long getStorageMaxSize() {
        return storageMaxSize;
    }

    private void checkFormats(String[] formats, long storageId) {
        for (String format : formats) {
            if (!format.equals("txt") && !format.equals("jpg")) {
                throw new IllegalArgumentException("Creation of storage with ID: " + storageId + " failed. Storage can support either txt or jpg format, you are trying to assign illegal storage format");
            }
        }
    }

    @Override
    public String toString() {
        return "Storage{" +
                "id=" + id +
                ", formatsSupported=" + Arrays.toString(formatsSupported) +
                ", storageCountry='" + storageCountry + '\'' +
                ", storageMaxSize=" + storageMaxSize +
                '}';
    }

}
