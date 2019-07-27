import mne
import matplotlib.pyplot as plt
import numpy as np
from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split, GridSearchCV
from sklearn.neural_network import MLPClassifier
from sklearn.pipeline import make_pipeline
from sklearn.preprocessing import MinMaxScaler
from sklearn.svm import SVC
from sklearn.calibration import calibration_curve

import utils


def compare(experiences):

    X, y = utils.flatten_experiences(experiences)

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.40, random_state=42)

    lr = (
        LogisticRegression(),
        {'C': [0.001, 0.01, 0.1, 1, 10],
         "penalty": ["l2"], "n_jobs": [-1],
         'max_iter': [300],
         "solver": ['lbfgs', 'saga']},
        'Logistic'
    )
    svc = (
        SVC(),
        {'C': [0.001, 0.01, 0.1, 1, 10]},
        'Support Vector Classification'
    )
    rfc = (
        RandomForestClassifier(),
        {
            'n_estimators': [1000],
            'max_features': ['auto', 'log2'],
            'criterion': ['gini', 'entropy'],
            "n_jobs": [-1]
        },
        'Random Forest'
    )
    mlp = (
        MLPClassifier(),
        {'solver': ['lbfgs'],
         'max_iter': [100],
         'alpha': 10.0 ** -np.arange(1, 10)
         # 'hidden_layer_sizes': np.arange(10, 15)
         },
        'MLP Classifier'
    )

    classifiers = [lr, svc, rfc, mlp]

    # #############################################################################
    # Plot calibration plots

    plt.figure(0, figsize=(10, 10))
    ax1 = plt.subplot2grid((3, 1), (0, 0), rowspan=2)
    ax2 = plt.subplot2grid((3, 1), (2, 0))

    ax1.plot([0, 1], [0, 1], "k:", label="Perfectly calibrated")

    best_classifier = None

    for classifier, param_grid, name in classifiers:
        clf, score = calibrate_classifier(X_train, X_test, y_train, y_test, classifier, param_grid, name)
        if best_classifier is None or best_classifier[1] < score:
            best_classifier = (clf, score)
        if hasattr(clf, "predict_proba"):
            prob_pos = clf.predict_proba(X_test)[:, 1]
        else:  # use decision function
            prob_pos = clf.decision_function(X_test)
            prob_pos = \
                (prob_pos - prob_pos.min()) / (prob_pos.max() - prob_pos.min())
        fraction_of_positives, mean_predicted_value = \
            calibration_curve(y_test, prob_pos, n_bins=10)

        ax1.plot(mean_predicted_value, fraction_of_positives, "s-",
                 label="%s" % (name,))

        ax2.hist(prob_pos, range=(0, 1), bins=10, label=name,
                 histtype="step", lw=2)

    ax1.set_ylabel("Fraction of positives")
    ax1.set_ylim([-0.05, 1.05])
    ax1.legend(loc="lower right")
    ax1.set_title('Calibration plots  (reliability curve)')

    ax2.set_xlabel("Mean predicted value")
    ax2.set_ylabel("Count")
    ax2.legend(loc="upper center", ncol=2)

    plt.tight_layout()
    plt.show()

    print("Best all around classifier: {} with score: {}".format(best_classifier[0]._final_estimator.best_estimator_,
                                                                 best_classifier[1]))

    return best_classifier[0], best_classifier[1]


def calibrate_classifier(X_train, X_test, y_train, y_test, classifier, param_grid, classifier_name):

    pipeline = make_pipeline(
        mne.decoding.Vectorizer(),  # Transform n-dimensional array into 2D array of n_samples by n_features.
        MinMaxScaler(),  # Transforms features by scaling each feature to a given range (0, 1).
        GridSearchCV(classifier,
                     param_grid,
                     cv=10, refit=True)
    )

    classifier_fit = pipeline.fit(X_train, y_train)
    best_estimator = classifier_fit._final_estimator.best_estimator_
    best_score = classifier_fit._final_estimator.best_score_
    print("Best {}:\n{} with score: {}".format(classifier_name, best_estimator, best_score))
    test_score = classifier_fit.score(X_test, y_test)
    print("Test score: ", test_score)

    return pipeline, best_score
