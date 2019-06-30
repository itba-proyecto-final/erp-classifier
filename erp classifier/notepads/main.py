import trainer
import tester

experiences_list = trainer.get_epochs_data_list(
    ['../data/grid_lights/nati/record-bv-generic-nati-[2019.04.27-19.11.05].vhdr'])

train_experiences = experiences_list[:int(len(experiences_list)*.8)]
test_experiences = experiences_list[int(len(experiences_list)*.8):]

classifier = trainer.train_classifier(train_experiences, "../classifiers/nati.joblib")
classification = tester.test_classifier("../classifiers/nati.joblib", test_experiences)

