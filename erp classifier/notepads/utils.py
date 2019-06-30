def flatten_experiences(experiences):
    """
    Given a list of epochs data list and a list of label lists,
    it returns a list containing all epochs data and another one containing all labels.
    :param experiences: list of list of epochs with labels
    :return: a list containing all epochs data and another one containing all labels
    """

    samples = [sample for experience in experiences for sample in experience]
    epochs_data = [epoch for epoch,_ in samples]
    labels = [label for _, label in samples]
    return epochs_data, labels
