# Copyright Valkey GLIDE Project Contributors - SPDX Identifier: Apache-2.0
"""
module for `vector search` commands.
"""

from typing import List, Optional, cast

from glide.async_commands.server_modules.ft_options.ft_constants import (
    CommandNames,
    FtCreateKeywords,
)
from glide.async_commands.server_modules.ft_options.ft_create_options import (
    Field,
    FtCreateOptions,
)
from glide.constants import TOK, TEncodable
from glide.glide_client import TGlideClient


async def create(
    client: TGlideClient,
    indexName: TEncodable,
    schema: List[Field],
    options: Optional[FtCreateOptions] = None,
) -> TOK:
    """
    Creates an index and initiates a backfill of that index.

    Args:
        client (TGlideClient): The client to execute the command.
        indexName (TEncodable): The index name for the index to be created
        schema (List[Field]): The fields of the index schema, specifying the fields and their types.
        options (Optional[FtCreateOptions]): Optional arguments for the FT.CREATE command. See `FtCreateOptions`.

    Returns:
        TOK: A simple "OK" response.

    Examples:
        >>> from glide.async_commands.server_modules import ft
        >>> schema: List[Field] = []
        >>> field: TextField = TextField("title")
        >>> schema.append(field)
        >>> prefixes: List[str] = []
        >>> prefixes.append("blog:post:")
        >>> index = "idx"
        >>> result = await ft.create(glide_client, index, schema, FtCreateOptions(DataType.HASH, prefixes))
            'OK'  # Indicates successful creation of index named 'idx'
    """
    args: List[TEncodable] = [CommandNames.FT_CREATE, indexName]
    if options:
        args.extend(options.toArgs())
    if schema:
        args.append(FtCreateKeywords.SCHEMA)
        for field in schema:
            args.extend(field.toArgs())
    return cast(TOK, await client.custom_command(args))


async def dropindex(client: TGlideClient, indexName: TEncodable) -> TOK:
    """
    Drops an index. The index definition and associated content are deleted. Keys are unaffected.

    Args:
        client (TGlideClient): The client to execute the command.
        indexName (TEncodable): The index name for the index to be dropped.

    Returns:
        TOK: A simple "OK" response.

    Examples:
        For the following example to work, an index named 'idx' must be already created. If not created, you will get an error.
        >>> from glide.async_commands.server_modules import ft
        >>> indexName = "idx"
        >>> result = await ft.dropindex(glide_client, indexName)
            'OK'  # Indicates successful deletion/dropping of index named 'idx'
    """
    args: List[TEncodable] = [CommandNames.FT_DROPINDEX, indexName]
    return cast(TOK, await client.custom_command(args))


async def aliasadd(
    client: TGlideClient, alias: TEncodable, indexName: TEncodable
) -> TOK:
    """
    Adds an alias for an index. The new alias name can be used anywhere that an index name is required.

    Args:
        client (TGlideClient): The client to execute the command.
        alias (TEncodable): The alias to be added to an index.
        indexName (TEncodable): The index name for which the alias has to be added.

    Returns:
        TOK: A simple "OK" response.

    Examples:
        >>> from glide.async_commands.server_modules import ft
        >>> result = await ft.aliasadd(glide_client, "myalias", "myindex")
            'OK'  # Indicates the successful addition of the alias named "myalias" for the index.
    """
    args: List[TEncodable] = [CommandNames.FT_ALIASADD, alias, indexName]
    return cast(TOK, await client.custom_command(args))


async def aliasdel(client: TGlideClient, alias: TEncodable) -> TOK:
    """
    Deletes an existing alias for an index.

    Args:
        client (TGlideClient): The client to execute the command.
        alias (TEncodable): The existing alias to be deleted for an index.

    Returns:
        TOK: A simple "OK" response.

    Examples:
        >>> from glide.async_commands.server_modules import ft
        >>> result = await ft.aliasdel(glide_client, "myalias")
            'OK'  # Indicates the successful deletion of the alias named "myalias"
    """
    args: List[TEncodable] = [CommandNames.FT_ALIASDEL, alias]
    return cast(TOK, await client.custom_command(args))


async def aliasupdate(
    client: TGlideClient, alias: TEncodable, indexName: TEncodable
) -> TOK:
    """
    Updates an existing alias to point to a different physical index. This command only affects future references to the alias.

    Args:
        client (TGlideClient): The client to execute the command.
        alias (TEncodable): The alias name. This alias will now be pointed to a different index.
        indexName (TEncodable): The index name for which an existing alias has to updated.

    Returns:
        TOK: A simple "OK" response.

    Examples:
        >>> from glide.async_commands.server_modules import ft
        >>> result = await ft.aliasupdate(glide_client, "myalias", "myindex")
            'OK'  # Indicates the successful update of the alias to point to the index named "myindex"
    """
    args: List[TEncodable] = [CommandNames.FT_ALIASUPDATE, alias, indexName]
    return cast(TOK, await client.custom_command(args))
