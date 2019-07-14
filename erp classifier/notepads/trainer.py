import mne
import numpy as np
import joblib
from sklearn.pipeline import make_pipeline
from sklearn.preprocessing import MinMaxScaler
from sklearn.linear_model import LogisticRegression

import utils

EVENTS_IDS = {'Closer': 1, 'Further': 2, 'Start': 3, 'Finish': 4}
EVENTS_TMIN = 0.0
EVENTS_TMAX = 2.0


def get_experiences(file):
    """
    :param file: file that will be used to generate epochs and its corresponding labels.
    :return: the epochs and its corresponding labels.
    :rtype: tuple(matrix, list)
    """

    montage = 'standard_1020'
    sig_mne = mne.io.read_raw_brainvision(file, montage, preload=True)
    sig_mne.pick_types(eeg=True, stim=True)  # only use eeg and stim channels

    # Do not set reference
    # https://martinos.org/mne/dev/auto_tutorials/plot_eeg_erp.html#setting-eeg-reference
    # If an empty list is specified, the data is assumed to already have a proper reference
    # and MNE will not attempt any re-referencing of the data
    sig_mne, _ = mne.set_eeg_reference(sig_mne, [])

    stim_channel_name = 'STI 014'
    # Find events
    # https://martinos.org/mne/stable/generated/mne.find_events.html
    events = mne.find_events(sig_mne, stim_channel=stim_channel_name, output='onset', consecutive=True,
                             min_duration=0, shortest_event=1, mask=None, uint_cast=False, mask_type='and',
                             initial_event=True, verbose=None)

    epochs = mne.Epochs(sig_mne, events, EVENTS_IDS, tmin=EVENTS_TMIN, tmax=EVENTS_TMAX, baseline=(None, 0),
                        picks=None, preload=True, reject=None, flat=None, proj=False, decim=1,
                        reject_tmin=None, reject_tmax=None, detrend=None, on_missing='error',
                        reject_by_annotation=True, metadata=None, verbose=None)
    epochs_labels = epochs.events[:, -1]

    starts = np.argwhere(epochs_labels == 3).flatten()
    finishes = np.argwhere(epochs_labels == 4).flatten()

    further_label = 2
    closer_label = 1
    hit_epochs_labels = np.array([1 if x == further_label else 0 if x == closer_label else x for x in epochs_labels])

    experiences = []

    for start, finish in zip(starts, finishes):
        experiences.append(utils.deep_zip(epochs.get_data()[start+1:finish, :8, 1:], hit_epochs_labels[start+1:finish]))

    return experiences


def get_epochs_data_list(file_list):
    experiences_list = list()
    for file in file_list:
        experiences = get_experiences(file)
        [experiences_list.append(experience) for experience in experiences]
    return experiences_list


def train_classifier(experiences, classifier_path, scikit_classifier=LogisticRegression()):
    """
    TODO: hacer bien
    :param experiences:
    :param classifier_path:
    :param scikit_classifier:
    :return:
    """

    epochs_data, labels = utils.flatten_experiences(experiences)
    classifier = make_pipeline(
        mne.decoding.Vectorizer(),  # Transform n-dimensional array into 2D array of n_samples by n_features.
        MinMaxScaler(),  # Transforms features by scaling each feature to a given range (0, 1).
        scikit_classifier  # linear model for classification
    )
    classifier_fit = classifier.fit(epochs_data, labels)
    joblib.dump(classifier_fit, classifier_path)
    return classifier_fit


# if __name__ == "__main__":
    # epochs, epochs_labels = get_epochs_data(
    #     '../data/grid_lights/nati/record-bv-generic-nati-[2019.04.27-19.11.05].vhdr')
    # epochs, epochs_labels = flatten_epochs_data([epochs, epochs], [epochs_labels, epochs_labels])
    # print(epochs)
    # print(epochs_labels)
    # train_classifier(epochs, epochs_labels, "../classifiers/hola.joblib", LogisticRegression())
