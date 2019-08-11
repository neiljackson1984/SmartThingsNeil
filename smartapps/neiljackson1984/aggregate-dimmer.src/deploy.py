import argparse
import os

parser = argparse.ArgumentParser(description="Upload app or driver code to the hubitat")
parser.add_argument("--source", action='store', nargs=1, required=True, help="the file to be uploaded to the hubitat.")
args = parser.parse_args()
print("source is " + str(args.source))
print("os.getcwd(): " + os.getcwd())


# read parameters from the magic comment strings in the source file