from datetime import datetime, timedelta
from enum import Enum
from typing import List, Type, Union, get_args

from pybushka.constants import TResult
from pybushka.protobuf.redis_request_pb2 import RequestType


class ConditionalSet(Enum):
    """SET option: A condition to the "SET" command.
    - ONLY_IF_EXISTS - Only set the key if it already exist. Equivalent to `XX` in the Redis API
    - ONLY_IF_DOES_NOT_EXIST - Only set the key if it does not already exist. Equivalent to `NX` in the Redis API
    """

    ONLY_IF_EXISTS = 0  # Equivalent to `XX` in the Redis API
    ONLY_IF_DOES_NOT_EXIST = 1  # Equivalent to `NX` in the Redis API


class ExpiryType(Enum):
    """SET option: The type of the expiry.
    - SEC - Set the specified expire time, in seconds. Equivalent to `EX` in the Redis API.
    - MILLSEC - Set the specified expire time, in milliseconds. Equivalent to `PX` in the Redis API.
    - UNIX_SEC - Set the specified Unix time at which the key will expire, in seconds. Equivalent to `EXAT` in the Redis API.
    - UNIX_MILLSEC - Set the specified Unix time at which the key will expire, in milliseconds. Equivalent to `PXAT` in the
        Redis API.
    - KEEP_TTL - Retain the time to live associated with the key. Equivalent to `KEEPTTL` in the Redis API.
    """

    SEC = 0, Union[int, timedelta]  # Equivalent to `EX` in the Redis API
    MILLSEC = 1, Union[int, timedelta]  # Equivalent to `PX` in the Redis API
    UNIX_SEC = 2, Union[int, datetime]  # Equivalent to `EXAT` in the Redis API
    UNIX_MILLSEC = 3, Union[int, datetime]  # Equivalent to `PXAT` in the Redis API
    KEEP_TTL = 4, Type[None]  # Equivalent to `KEEPTTL` in the Redis API


class ExpirySet:
    """SET option: Represents the expiry type and value to be executed with "SET" command."""

    def __init__(
        self,
        expiry_type: ExpiryType,
        value: Union[int, datetime, timedelta, None],
    ) -> None:
        """
        Args:
            - expiry_type (ExpiryType): The expiry type.
            - value (Union[int, datetime, timedelta, None]): The value of the expiration type. The type of expiration
                determines the type of expiration value:
                - SEC: Union[int, timedelta]
                - MILLSEC: Union[int, timedelta]
                - UNIX_SEC: Union[int, datetime]
                - UNIX_MILLSEC: Union[int, datetime]
                - KEEP_TTL: Type[None]
        """
        self.set_expiry_type_and_value(expiry_type, value)

    def set_expiry_type_and_value(
        self, expiry_type: ExpiryType, value: Union[int, datetime, timedelta, None]
    ):
        if not isinstance(value, get_args(expiry_type.value[1])):
            raise ValueError(
                f"The value of {expiry_type} should be of type {expiry_type.value[1]}"
            )
        self.expiry_type = expiry_type
        if self.expiry_type == ExpiryType.SEC:
            self.cmd_arg = "EX"
            if isinstance(value, timedelta):
                value = int(value.total_seconds())
        elif self.expiry_type == ExpiryType.MILLSEC:
            self.cmd_arg = "PX"
            if isinstance(value, timedelta):
                value = int(value.total_seconds() * 1000)
        elif self.expiry_type == ExpiryType.UNIX_SEC:
            self.cmd_arg = "EXAT"
            if isinstance(value, datetime):
                value = int(value.timestamp())
        elif self.expiry_type == ExpiryType.UNIX_MILLSEC:
            self.cmd_arg = "PXAT"
            if isinstance(value, datetime):
                value = int(value.timestamp() * 1000)
        elif self.expiry_type == ExpiryType.KEEP_TTL:
            self.cmd_arg = "KEEPTTL"
        self.value = str(value) if value else None

    def get_cmd_args(self) -> List[str]:
        return [self.cmd_arg] if self.value is None else [self.cmd_arg, self.value]


class FFICoreCommands:
    def get(self, key, *args, **kwargs):
        return self.execute_command("get", key, *args, **kwargs)

    def set(self, key, value, *args, **kwargs):
        return self.execute_command("set", key, value, *args, **kwargs)

    def get_direct(self, key):
        return self.connection.get(key)

    def set_direct(self, key, value):
        return self.connection.set(key, value)


class CoreCommands:
    async def set(
        self,
        key: str,
        value: str,
        conditional_set: Union[ConditionalSet, None] = None,
        expiry: Union[ExpirySet, None] = None,
        return_old_value: bool = False,
    ) -> TResult:
        """Set the given key with the given value. Return value is dependent on the passed options.
            See https://redis.io/commands/set/ for details.

            @example - Set "foo" to "bar" only if "foo" already exists, and set the key expiration to 5 seconds:

                connection.set("foo", "bar", conditional_set=ConditionalSet.ONLY_IF_EXISTS, expiry=Expiry(ExpiryType.SEC, 5))
        Args:
            key (str): the key to store.
            value (str): the value to store with the given key.
            conditional_set (Union[ConditionalSet, None], optional): set the key only if the given condition is met.
                Equivalent to [`XX` | `NX`] in the Redis API. Defaults to None.
            expiry (Union[Expiry, None], optional): set expiriation to the given key.
                Equivalent to [`EX` | `PX` | `EXAT` | `PXAT` | `KEEPTTL`] in the Redis API. Defaults to None.
            return_old_value (bool, optional): Return the old string stored at key, or None if key did not exist.
                An error is returned and SET aborted if the value stored at key is not a string.
                Equivalent to `GET` in the Redis API. Defaults to False.
        Returns:
            TRESULT:
                If the value is successfully set, return OK.
                If value isn't set because of only_if_exists or only_if_does_not_exist conditions, return None.
                If return_old_value is set, return the old value as a string.
        """
        args = [key, value]
        if conditional_set:
            if conditional_set == ConditionalSet.ONLY_IF_EXISTS:
                args.append("XX")
            if conditional_set == ConditionalSet.ONLY_IF_DOES_NOT_EXIST:
                args.append("NX")
        if return_old_value:
            args.append("GET")
        if expiry is not None:
            args.extend(expiry.get_cmd_args())
        return await self.execute_command(RequestType.SetString, args)

    async def get(self, key: str) -> Union[str, None]:
        """Get the value associated with the given key, or null if no such value exists.
         See https://redis.io/commands/get/ for details.

        Args:
            key (str): the key to retrieve from the database

        Returns:
            Union[str, None]: If the key exists, returns the value of the key as a string. Otherwise, return None.
        """
        return await self.execute_command(RequestType.GetString, [key])

    async def custom_command(self, command_args: List[str]) -> TResult:
        """Executes a single command, without checking inputs.
            @example - Return a list of all pub/sub clients:

                connection.customCommand(["CLIENT", "LIST","TYPE", "PUBSUB"])
        Args:
            command_args (List[str]): List of strings of the command's arguements.
            Every part of the command, including the command name and subcommands, should be added as a separate value in args.

        Returns:
            TResult: The returning value depends on the executed command
        """
        return await self.execute_command(RequestType.CustomCommand, command_args)