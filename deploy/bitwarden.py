# This library borrows from https://github.com/ewjoachim/bitwarden-keyring.  The
# main differences are that this is intended to be used as a standalone library
# rather than a keyring backend. AND that, within bw_args, we use
# shutil.which('bw') to explicitly lookup the absolute path of the bw executable
# rather than relying on subprocess.run() to find the executable autoamtically.
# In Windows, probably unlike in Mac/Linux for which the original
# bitwarden-keyring library was written, subprocess.run() does not do very well
# with unqualified executable paths.

import base64
import json
import os
import shutil
import subprocess
from typing import Optional


def user_is_authenticated():
    return bw_run(*bw_args("login", "--check")).returncode == 0


def bitwarden_cli_installed():
    return bool(shutil.which("bw"))


def ask_for_session(command):
    result = bw(command, "--raw")
    return result


def ask_for_session_command(is_authenticated):
    return "unlock" if is_authenticated else "login"


def wrong_password(output):
    if "Username or password is incorrect" in output:
        return True
    elif "Invalid master password" in output:
        return True
    return False


def bw_args(*args, session=None):
    # cli_args = ["bw"]
    cli_args = [shutil.which("bw")]
    if session:
        cli_args += ["--session", session]

    return cli_args + list(args)


def bw_run(*args):
    return subprocess.run(args, stdout=subprocess.PIPE, check=True)


def bw(*args, session=None):

    cli_args = bw_args(*args, session=session)

    while True:
        try:
            result = bw_run(*cli_args).stdout.strip()
        except subprocess.CalledProcessError as exc:
            output = exc.stdout.decode("utf-8")
            if wrong_password(output):
                print(output)
                continue
            raise ValueError(output) from exc
        else:
            break

    return result



def get_session(environ):
    if "BW_SESSION" in environ:
        try:
            # Check that the token works.
            bw("sync")
        except ValueError:
            pass
        else:
            return environ["BW_SESSION"]

    command = ask_for_session_command(is_authenticated=user_is_authenticated())
    return ask_for_session(command=command)


def getBitwardenItem(idOfBitwardenItem : str) -> dict:
    session = get_session(os.environ)
    result = bw("get", "item", idOfBitwardenItem, session=session)
    bitwardenItem = json.loads(result)
    return bitwardenItem