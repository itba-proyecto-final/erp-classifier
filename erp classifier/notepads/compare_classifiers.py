import mne
import matplotlib.pyplot as plt
from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split, GridSearchCV
from sklearn.neural_network import MLPClassifier
from sklearn.pipeline import make_pipeline
from sklearn.preprocessing import MinMaxScaler
from sklearn.svm import LinearSVC
from sklearn.calibration import calibration_curve

import utils


def compare(experiences):
    X, y = utils.flatten_experiences(experiences)

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.40, random_state=42)

    lr = (
        LogisticRegression(),
        {'C': [x/10 for x in range(1, 21)],
         "penalty": ["l2"], "n_jobs": [-1],
         "solver": ['newton-cg', 'lbfgs', 'liblinear', 'sag', 'saga']},
        'Logistic'
    )
    svc = (
        LinearSVC(),
        {'C': [90]},
        'Support Vector Classification'
    )
    rfc = (
        RandomForestClassifier(),
        {'n_estimators': [100]},
        'Random Forest'
    )
    mlp = (
        MLPClassifier(),
        {'alpha': [1],
         'max_iter': [1000]},
        'MLP Classifier'
    )

    classifiers = [lr, svc, rfc, mlp]

    # #############################################################################
    # Plot calibration plots

    plt.figure(figsize=(10, 10))
    ax1 = plt.subplot2grid((3, 1), (0, 0), rowspan=2)
    ax2 = plt.subplot2grid((3, 1), (2, 0))

    ax1.plot([0, 1], [0, 1], "k:", label="Perfectly calibrated")
    for classifier, param_grid, name in classifiers:
        # clf.fit(X_train, y_train)
        clf = calibrate_classifier(X_train, X_test, y_train, y_test, classifier, param_grid)
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


def calibrate_classifier(X_train, X_test, y_train, y_test, classifier, param_grid):

    pipeline = make_pipeline(
        mne.decoding.Vectorizer(),  # Transform n-dimensional array into 2D array of n_samples by n_features.
        MinMaxScaler(),  # Transforms features by scaling each feature to a given range (0, 1).
        GridSearchCV(classifier,
                     param_grid,
                     cv=2, refit=True)
    )

    classifier_fit = pipeline.fit(X_train, y_train)
    best_estimator = classifier_fit._final_estimator.best_estimator_
    print("Best estimator:\n{}".format(best_estimator))
    print("score", classifier_fit.score(X_test, y_test))

    return pipeline
