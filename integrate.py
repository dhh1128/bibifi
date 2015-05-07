#! /usr/bin/python

import os, sys, subprocess, re, traceback, smtplib, shlex

my_folder = os.path.dirname(os.path.abspath(__file__))
hash_pat = '[a-fA-F0-9]{40}'
commit_pat = re.compile(r'^\s*commit\s+(%s)\s*$' % hash_pat, re.MULTILINE)
status_pat = re.compile(r'\s*(%s)\s+([a-zA-Z]+)' % hash_pat)
to_addr = 'daniel.hardman@gmail.com'
from_addr = 'seada@builditbreakit.org'
status_file = os.path.join(my_folder, 'integrate-status.txt')

def get_latest_commit_id():
    answer = subprocess.check_output(shlex.split('git log -1'))
    m = commit_pat.search(answer)
    return m.group(1)
    
def report(subject, msg):
    smtpserver = smtplib.SMTP('localhost', 25)
    header = 'To: %s\nFrom: "SEADA bibifi vm" <%s>\nSubject: %s\n' % (to_addr, from_addr, subject)
    msg = header + '\n' + msg
    print(msg)
    smtpserver.sendmail('seada@builditbreakit.org', to_addr, msg)
    smtpserver.close()
    
def get_integrate_status():
    if os.path.isfile(status_file):
        with open(status_file, 'r') as f:
            txt = f.read()
        m = status_pat.search(txt)
        if m:
            return (m.group(1), m.group(2))
    return ('no commit id', 'unknown')

def set_integrate_status(commit_id, status):
    with open(status_file, 'w') as f:
        f.write('%s %s\n' % (commit_id, status))

def read_f(fpath):
    with open(fpath, 'r') as f:
        return f.read()
    os.remove(fpath)

def integrate():
    os.chdir(my_folder)
    new_status = 'error'
    new_commit_id = 'unknown'
    try:
        try:
            old_commmit_id, old_status = get_integrate_status()
            current_commit_id = get_latest_commit_id()
            subprocess.check_call(shlex.split('git pull'))
            new_commit_id = get_latest_commit_id()
            if current_commit_id == new_commit_id and old_commmit_id == new_commit_id:
                #print('No commits since last integrate.')
                return
            else:
                if current_commmit_id != new_commit_id:
                    # We might have a new integrate script; run it instead.
                    os.system('./integrate.py')
                    return
            
            my_stdout = open('.make.stdout', 'w')
            my_stderr = open('.make.stderr', 'w')
            child = subprocess.Popen(shlex.split('make -C build'), stdout=my_stdout, stderr=my_stderr)
            child.communicate()
            my_stdout.close()
            my_stderr.close()
            my_stdout = read_f('.make.stdout')
            my_stderr = read_f('.make.stderr')
            
            if child.returncode == 0:
                new_status = 'OK'
                if old_status == 'OK':
                    #print('Build is still clean.')
                    return
                subject = 'build is fixed (commit %s...)' % (new_commit_id[:10])
                msg = my_stdout
            else:
                new_status = 'fail'
                subject = 'build is still broken (commit %s...)' % (new_commit_id[:10])
                if not my_stdout:
                    my_stdout = '(no stdout)'
                if not my_stderr:
                    my_stderr = '(no stderr)'
                msg = 'make returned %d:\n\n%s\n%s' % (child.returncode, my_stdout, my_stderr)
            report(subject, msg)
        except:
            new_status = 'error'
            new_commit_id = 'unknown'
            msg = traceback.format_exc()
            report('unable to integrate', msg)
    finally:
        os.chdir(my_folder)
        set_integrate_status(new_commit_id, new_status)

if __name__ == '__main__':
    integrate()