import subprocess
import os
import argparse

script_dir_path = os.path.dirname(os.path.realpath(__file__))

args = {}

def stop_streaming():
    """Terminates the streaming and multicast processes and cleans up."""
    global stream_process, multicast_process

    if stream_process and stream_process.poll() is None:
        stream_process.terminate()
        stream_process.wait()
    
    if multicast_process and multicast_process.poll() is None:
        multicast_process.terminate()
        multicast_process.wait()

    subprocess.call(["pkill", "-9", "gst-launch"])
    subprocess.call(["pkill", "-9", "libcamera"])
    print("Processes cleaned up.")

def start_streaming():
    global stream_process, multicast_process
    init_command = [f'{script_dir_path}/../init_stream_multicast.sh'] 

    stream_command = ["cargo", "run", "--release", "--", "--addr", args["server_addr"], "--token", \
    args["token"] if args["token"] is not None  else args["id"] 
    ]

    try:
        multicast_process = subprocess.Popen(init_command, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

        stream_process = subprocess.Popen(
            stream_command,
            cwd=f"{script_dir_path}/../ws-stream",
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            bufsize=1,
            universal_newlines=True
        )

        print("STREAM ON\n")
        # bascially:
        # keep printin until exception raises from process (interrupt or other)
        # in case both processes exit somehow it still gets cleaned up
        for line in iter(stream_process.stdout.readline, ''):
            print(line, end='')
    except Exception as e:
        print("Error:\n" + str(e))
    finally:
        stop_streaming()

def main():
    global args
    parser = argparse.ArgumentParser(description="Stream Controller")
    parser.add_argument("server_addr", help="Streaming server URL", default="ws://127.0.0.1")
    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument("--token", help="Authentication token")
    group.add_argument("--id", help="Stream ID")
    for (key, value) in parser.parse_args()._get_kwargs():
        args[key] = value
    

    print("Welcome to stream controller!")
    print("Press enter to toggle stream on and off.")

    while True:
        input("STREAM OFF\n")
        try:
            start_streaming()
        except KeyboardInterrupt:
            stop_streaming()
            exit(1)

if __name__ == "__main__":
    main()
