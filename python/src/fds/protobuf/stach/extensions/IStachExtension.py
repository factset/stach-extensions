from abc import abstractmethod, ABC


class IStachExtension(ABC):

    @abstractmethod
    def convert_to_dataframe(self):
        """
        Converts all the tables in the provided package object to list of data frames.

        :return: list of data frames

        """
