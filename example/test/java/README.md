Git global setup
git config --global user.name "chenzhong"
git config --global user.email "chenzhong@cpte.com"

Create a new repository
git clone git@afd5af08a46e:hlcs/hlcs-common.git
cd hlcs-common
touch README.md
git add README.md
git commit -m "add README"
git push -u origin master

Push an existing folder
cd existing_folder
git init
git remote add origin git@afd5af08a46e:hlcs/hlcs-common.git
git add .
git commit -m "Initial commit"
git push -u origin master
