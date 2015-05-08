#! /usr/bin/python

import os, sys, subprocess, re, traceback, smtplib, shlex

my_folder = os.path.dirname(os.path.abspath(__file__))
hash_pat = '[a-fA-F0-9]{40}'
commit_pat = re.compile(r'^\s*commit\s+(%s)\s*$' % hash_pat, re.MULTILINE)
status_pat = re.compile(r'\s*(%s)\s+([a-zA-Z]+)' % hash_pat)
to_addr = 'daniel.hardman@gmail.com'
from_addr = 'seada@builditbreakit.org'
status_file = os.path.join(my_folder, 'integrate-status.txt')

def trace(msg):
    pass #print(msg)

def get_latest_commit_id():
    answer = subprocess.check_output(shlex.split('git log -1'))
    m = commit_pat.search(answer)
    trace('latest commit id=%s' % m.group(1))
    return m.group(1)
    
def report(subject, msg, to='cybersecurity-capstone-2015@googlegroups.com'):
    smtpserver = smtplib.SMTP('localhost', 25)
    header = 'To: %s\nFrom: "SEADA bibifi vm" <%s>\nSubject: %s\n' % (to, from_addr, subject)
    msg = header + '\n' + msg
    trace(msg)
    smtpserver.sendmail('seada@builditbreakit.org', to, msg)
    smtpserver.close()
    
def get_integrate_status():
    id = 'no commit id'
    status = 'unknown'
    if os.path.isfile(status_file):
        with open(status_file, 'r') as f:
            txt = f.read()
        m = status_pat.search(txt)
        if m:
            id = m.group(1)
            status = m.group(2)
    trace('current integrate status: %s %s' % (id, status))
    return (id, status)

def set_integrate_status(commit_id, status):
    with open(status_file, 'w') as f:
        trace('writing new integrate status: %s %s' % (commit_id, status))
        f.write('%s %s\n' % (commit_id, status))

def read_f(fpath):
    with open(fpath, 'r') as f:
        txt = f.read()
    os.remove(fpath)
    return txt

def integrate():
    os.chdir(my_folder)
    new_status = 'error'
    new_commit_id = 'unknown'
    update_status = False
    try:
        try:
            old_commit_id, old_status = get_integrate_status()
            current_commit_id = get_latest_commit_id()
            subprocess.check_call(shlex.split('git pull'))
            new_commit_id = get_latest_commit_id()
            if current_commit_id == new_commit_id and old_commit_id == new_commit_id:
                trace('No commits since last integrate (commit %s).' % new_commit_id)
                return
            else:
                if current_commit_id != new_commit_id:
                    # We might have a new integrate script; run it instead.
                    trace('May have updated this script. Delegating work to new copy of me...')
                    os.system('./integrate.py')
                    return
            
            update_status = True
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
                    trace('Build is still clean.')
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
            report('unable to integrate', msg, to='daniel.hardman@gmail.com')
    finally:
        os.chdir(my_folder)
        if update_status:
            set_integrate_status(new_commit_id, new_status)

if __name__ == '__main__':
    integrate()
