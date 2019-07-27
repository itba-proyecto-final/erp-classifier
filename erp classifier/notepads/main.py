import compare_classifiers
import trainer
import tester
import utils

experiences_list = trainer.get_epochs_data_list(
    ['../data/grid_lights/marta/marta1/record-bv-generic-marta-[2019.06.15-19.34.00]].vhdr'])

# classifier = compare_classifiers.compare(experiences_list)

train_experiences = experiences_list[:3]
test_experiences = experiences_list[3:]
#
# X_train, y_train = utils.flatten_experiences(train_experiences)
# X_test, y_test = utils.flatten_experiences(test_experiences)

# X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.40, random_state=42)
#
classifier = trainer.train_classifier(train_experiences, '../classifiers/alex.joblib')
classification = tester.test_classifier(classifier, test_experiences)
#
state_files = ['../data/grid_lights/marta/marta2/grid_lights_experiment_marta_4',
               '../data/grid_lights/alex/alex2/grid_lights_experiment_marta_5']

utils.merge_predictions_with_states(classification, state_files, utils.grid_lights_rewards, '../rewards/marta.txt')
