package utils;

import java.io.File;
import java.io.FileNotFoundException;

public class FolderUtils {

    public static final String DATA_FOLDER = "data";
    public static final String RESULT_FOLDER = "result";

    public static String buildPath(boolean clearLast, String... path) {
        int len = path.length;

        int last = len - 1;

        String resultPath = RESULT_FOLDER + File.separator;
        resultPath += getClassName() + File.separator;

        File root = new File(resultPath);
        if (root.exists()) {
            if (!root.isDirectory()) {
                throw new RuntimeException(resultPath + " does not lead to a folder");
            }

            if (clearLast && last == -1) {
                removeFolder(root);
                createFolder(root);
            }

        } else {
            createFolder(root);
        }

        for (int i = 0; i < len; i++) {
            resultPath += path[i] + File.separator;

            File folder = new File(resultPath);
            if (folder.exists()) {
                if (!folder.isDirectory()) {
                    throw new RuntimeException(resultPath + " does not lead to a folder");
                }

                if (clearLast && i == last) {
                    removeFolder(folder);
                    createFolder(folder);
                }
            } else {
                createFolder(folder);
            }
        }
        return resultPath;
    }

    public static String clearOrCreate(String... path) {
        return buildPath(true, path);
    }

    public static void createFolder(File folder) {
        if (!folder.mkdirs()) {
            throw new RuntimeException("Can't create folder " + folder);
        }
    }

    public static String getClassName() {
        StackTraceElement[] elements = Thread.getAllStackTraces().get(Thread.currentThread());
        return elements[elements.length - 1].getClassName();
    }

    public static void main(String[] args) throws FileNotFoundException {
        System.out.println(buildPath(true, "test"));
        System.out.println(openData("cos.png"));
        System.out.println(new File(openData("cos.png")).length());
    }

    public static String openData(String... path) throws FileNotFoundException {
        int len = path.length;
        int last = len - 1;
        String dataPath = DATA_FOLDER + File.separator;
        dataPath += getClassName() + File.separator;

        File root = new File(dataPath);
        if (root.exists()) {
            if (!root.isDirectory()) {
                throw new RuntimeException(dataPath + " does not lead to a folder");
            }
        } else {
            createFolder(root);
        }

        for (int i = 0; i < len; i++) {
            dataPath += path[i];

            File file = new File(dataPath);
            if (file.exists()) {
                if (file.isDirectory()) {
                    dataPath += File.separator;
                } else {
                    if (i != last) {
                        throw new RuntimeException(dataPath + " does not lead to a folder");
                    }
                }
            } else {
                throw new FileNotFoundException("Can't find " + file);
            }
        }
        return dataPath;
    }

    public static String openOrCreate(String... path) {
        return buildPath(false, path);
    }

    public static void removeFile(File file) {
        if (!file.delete()) {
            throw new RuntimeException("Can't remove file " + file);
        }
    }

    public static void removeFolder(File folder) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                removeFolder(file);
            } else {
                removeFile(file);
            }
        }

        if (!folder.delete()) {
            throw new RuntimeException("Can't remove folder " + folder);
        }
    }
}
