import keyring
import bitwarden_keyring


# def monkeypatched_bw_run(*args): return bitwarden_keyring.subprocess.run(args, stdout=bitwarden_keyring.subprocess.PIPE, check=True, shell=True)
# bitwarden_keyring.bw_run = monkeypatched_bw_run 

# The bitwarden_keyring module assumes that subprocess.run() will be able to find
# the bw executable when only given "bw" as the first element in args.  This
# might be a safe assumption in the Mac/Ubuntu platform for which
# bitwarden_keyring was designed, but it does not seem to hold on Windows (see
# https://stackoverflow.com/questions/5658622/python-subprocess-popen-environment-path).
#
# The below monkeypatched replacement for bitwarden_keyring.bw_args() fixes the issue by 
# looking up the full absolute path to the bw executable and passing that full path, rather than "bw",
# as the first element of the args argument to subprocess run.
def monkeypatched_bw_args(*args, session=None):
    # cli_args = ["bw"]
    
    import shutil
    cli_args = [shutil.which("bw")]

    if session:
        cli_args += ["--session", session]
    return cli_args + list(args)
bitwarden_keyring.bw_args = monkeypatched_bw_args

b=keyring.get_credential(service_name="hubitat1.n.rattnow.com", username="neil")
# that worked.


#none of the below work:
c=keyring.get_credential(service_name="hubitat1.n.rattnow.com", username=None)
f=keyring.get_credential(service_name="hubitat1.n.rattnow.com", username="")
g=keyring.get_credential(service_name="hubitat1.n.rattnow.com", username="*")

d=keyring.get_credential(service_name="df35abce-d3b0-4a46-ad24-aa010046e857", username="neil")
e=keyring.get_credential(service_name="df35abce-d3b0-4a46-ad24-aa010046e857", username=None)


# I might be better off calling bitwarden cli directly.
print(f"c: {c}")
