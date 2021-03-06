import csv
import numpy as np
from scipy.io import arff
import pandas as pd
from pathlib import Path
from tqdm import tqdm
from tqdm import trange


def prepare_data(min_instances: int) -> [(str, int)]:
    with open('./datasets/datasets.csv') as csv_file:
        csv_reader = csv.reader(csv_file, delimiter=',')
        line = 0
        goodIds = []
        # csv format [DS_ID,NumberOfInstances,NumberOfFeatures,NumberOfClasses,Target_Feature,DS_URL]
        for row in csv_reader:
            if line != 0:
                d_id = row[0]
                #print(type(row[1]))
                instances = int(float(row[1]))
                features = int(float(row[2]))
                classes = int(float(row[3]))
                target = int(float(row[4]))
                if classes > 0:
                    goodIds.append((d_id, target))
            line += 1
        #print(goodIds)
        return goodIds


def prepare_col(col_in: np.ndarray, tpe: str) -> [[float]]:
    col = np.nan_to_num(col_in)
    if 'f' in tpe:
        return np.reshape(col, (-1, 1)).tolist()
    else:
        from sklearn import preprocessing
        le = preprocessing.LabelEncoder()
        le.fit(col)
        new_col = np.reshape(le.transform(col), (-1, 1))
        from sklearn.preprocessing import OneHotEncoder
        enc = OneHotEncoder()
        enc.fit(new_col)
        res_col = enc.transform(new_col).toarray()
        return res_col.tolist()


def prepare_class(col_in: np.ndarray, tpe: str) -> [[float]]:
    col = np.nan_to_num(col_in)
    if 'f' in tpe:
        return np.reshape(col, (-1, 1)).tolist()
    else:
        from sklearn import preprocessing
        le = preprocessing.LabelEncoder()
        le.fit(col)
        new_col = np.reshape(le.transform(col), (-1, 1))
        return new_col.tolist()


def shorted_data(data):
    if data.shape[0] > 5000:
        return data[np.random.choice(len(data), size=5000, replace=False)]
    else:
        return data


# creates array data[instances max 5000][features], class[col]
def load_dataset(name_in: str, class_col_num: int) -> (np.ndarray, np.ndarray):
    path = f'{data_path}/{name_in}{file_extension}'
    data_in, meta = arff.loadarff(path)
    data = shorted_data(data_in)
    new_data = []
    class_col = []
    if class_col_num == -1:
        class_col_num = len(data.dtype.descr) - 1
    for i, (name, tpe) in tqdm(enumerate(data.dtype.descr), total=len(data.dtype.descr)):
        if i != class_col_num:
            prepared = prepare_col(data[name], tpe)
            if i == 0 or (class_col_num == 0 and i == 1):
                new_data.extend(prepared)
            else:
                for j, d in enumerate(prepared):
                    new_data[j].extend(d)
        else:
            class_col = prepare_class(data[name], tpe)

    return np.array(new_data), np.array(class_col)


if __name__ == '__main__':
    min_instances = 50
    datasets = prepare_data(min_instances)
    data_path = "tmp"
    file_extension = ".arff"
    save_prefix = "./datasets/np_raw/"
    start_from = 1
    target_name = ["40918", "40920", "40976", "40992", "40993", "41022", "41039", "41091", "41170", "41197"]

    for i, (name, cl_col) in tqdm(enumerate(datasets), total=len(datasets)):
        if i >= start_from or name in target_name:
            try:
                print(name)
                path = Path(f'{save_prefix}{name}/')
                path.mkdir(parents=True, exist_ok=True)

                data_read, cl = load_dataset(name, cl_col)
                np.save(f'{save_prefix}{name}/{name}_data', data_read)
                np.save(f'{save_prefix}{name}/{name}_class', cl)
            except Exception as e:
                print("Error", name, str(e))
                path = Path(f'{save_prefix}{name}/')
                path.mkdir(parents=True, exist_ok=True)
                path.rmdir()
