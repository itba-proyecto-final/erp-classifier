import matplotlib
import matplotlib.pyplot as plt
import joblib
import mne
import numpy as np
from sklearn.metrics import roc_curve, auc, accuracy_score, confusion_matrix, classification_report
from scipy import interp

import utils


def plot_prediction_results(prediction_proba, labels_test):
    fprs = np.linspace(0, 1, 100)

    # ROC curve for fold i
    # https://scikit-learn.org/stable/auto_examples/model_selection/plot_roc_crossval.html
    fpr, tpr, thresholds = roc_curve(labels_test, prediction_proba[:, 1])
    tprs = interp(fprs, fpr, tpr)
    tprs[0] = 0.0
    tprs[-1] = 1.0
    roc_auc = auc(fprs, tprs)
    std_auc = np.std(roc_auc)

    plt.figure(1, figsize=(20, 10))
    plt.plot(fprs, tprs, color='b', label=r'ROC (AUC = %0.2f)' % roc_auc,
             lw=2, alpha=.8)

    # Chance line
    plt.plot([0, 1], [0, 1], linestyle='--', lw=2, color='r', label='Chance', alpha=.8)

    plt.title('ROC curve')
    plt.xlabel('False positive rate')
    plt.xlim([-0.05, 1.05])
    plt.ylabel('True positive rate')
    plt.ylim([-0.05, 1.05])
    plt.legend(loc="best")
    plt.show()


def plot_confusion_matrix(prediction, labels_test):
    confusion_m = confusion_matrix(labels_test, prediction)
    print("Confusion matrix")
    print(confusion_m)

    # Normalized confusion matrix
    confusion_m_norm = confusion_m.astype(float) / confusion_m.sum(axis=1)[:, np.newaxis]

    target_names = ['noerror', 'error']

    # Classification report
    report = classification_report(labels_test, prediction, target_names=target_names)
    print(report)

    # Plot it
    plt.figure(2, figsize=(20, 10))
    plt.imshow(confusion_m_norm, interpolation='nearest', cmap=plt.cm.Blues)
    tick_marks = np.arange(len(target_names))
    plt.title('Normalized Confusion matrix')
    plt.xlabel('Predicted label')
    plt.xticks(tick_marks, target_names, rotation=45)
    plt.ylabel('True label')
    plt.yticks(tick_marks, target_names)
    plt.clim(0, 1)
    plt.colorbar()
    mne.viz.tight_layout()
    plt.show()


def test_classifier_from_file(classifier_path, test_experiences):
    classifier = joblib.load(classifier_path)
    test_classifier(classifier, test_experiences)


def test_classifier(classifier, test_experiences):
    testing_epochs_data, testing_labels = utils.flatten_experiences(test_experiences)

    prediction = classifier.predict(testing_epochs_data)
    print("Accuracy: {}".format(accuracy_score(testing_labels, prediction)))

    prediction_proba = classifier.predict_proba(testing_epochs_data)

    font = {'size': 22}
    matplotlib.rc('font', **font)

    plot_prediction_results(prediction_proba, testing_labels)
    plot_confusion_matrix(prediction, testing_labels)
    return prediction
