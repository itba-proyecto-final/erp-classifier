import collections


def deep_zip(*args):
    if all(isinstance(arg, collections.Iterable) for arg in args):
        return [deep_zip(*vals) for vals in zip(*args)]
    return args


def flatten_experiences(experiences):
    """
    Given a list of epochs data list and a list of label lists,
    it returns a list containing all epochs data and another one containing all labels.
    :param experiences: list of list of epochs with labels
    :return: a list containing all epochs data and another one containing all labels
    """

    samples = [sample for experience in experiences for sample in experience]
    epochs_data = [epoch for epoch, _ in samples]
    labels = [label for _, label in samples]
    return epochs_data, labels


def grid_lights_rewards(prediction):
    if prediction is None:
        return 0
    else:
        return -1 * prediction


def prediction_2_reward(predictions, reward_function):
    return reward_function(predictions)


def state_reward_merge(state_files, rewards, reward_function, output_file):
    rewards = list(rewards)
    with open(output_file, "w") as out:
        experiment_name = None
        file_content = ""
        for state_file in state_files:
            with open(state_file, "r") as inp:
                if experiment_name is None:
                    experiment_name = inp.readline()
                    file_content += experiment_name
                elif experiment_name != inp.readline():
                    raise Exception("Experiments are different.")

                is_first_state = True

                for line in inp:
                    if line != "\n":
                        file_content += line
                    else:
                        if is_first_state:
                            is_first_state = False
                            file_content += str(reward_function(0)) + "\n"
                        else:
                            file_content += str(rewards.pop(0)) + "\n"
                file_content += "\n" + str(reward_function(None)) + "\n--\n"

        out.write(file_content)


def merge_predictions_with_states(predictions, state_files, reward_function, output_file):
    rewards = prediction_2_reward(predictions, reward_function)
    state_reward_merge(state_files, rewards, reward_function, output_file)
