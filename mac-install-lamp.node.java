MAMP instructions
https://echo.co/blog/os-x-109-local-development-environment-apache-php-and-mysql-homebrew
Grunt instructions
http://gruntjs.com/getting-started

# Check Java
java --version
# No Java runtime present, requesting install.
# http://support.apple.com/downloads/DL1572/en_US/JavaForOSX2014-001.dmg
# Open JDK?
# https://wiki.openjdk.java.net/display/BSDPort/Main

# Get Xcode and install it
# https://developer.apple.com/downloads/index.action#

# Install brew
ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
brew doctor
brew update

# Install git
brew install git

# Update path
echo "export PATH=\$(echo \$PATH | sed 's|/usr/local/bin||; s|/usr/local/sbin||; s|::|:|; s|^:||; s|\(.*\)|/usr/local/bin:/usr/local/sbin:\1|')" >> ~/.bash_profile && source ~/.bash_profile

# Install MySQL
brew install -v mysql

# Configure MySQL
cp -v $(brew --prefix mysql)/support-files/my-default.cnf $(brew --prefix mysql)/my.cnf
cat >> $(brew --prefix mysql)/my.cnf <<'EOF'
# Echo & Co. changes
max_allowed_packet = 2G
innodb_file_per_table = 1
EOF
sed -i '' 's/^# \(innodb_buffer_pool_size\)/\1/' $(brew --prefix mysql)/my.cnf

# MySQL launch
[ ! -d ~/Library/LaunchAgents ] && mkdir -v ~/Library/LaunchAgents
 
[ -f $(brew --prefix mysql)/homebrew.mxcl.mysql.plist ] && ln -sfv $(brew --prefix mysql)/homebrew.mxcl.mysql.plist ~/Library/LaunchAgents/
 
[ -e ~/Library/LaunchAgents/homebrew.mxcl.mysql.plist ] && launchctl load -w ~/Library/LaunchAgents/homebrew.mxcl.mysql.plist

# Start Apache
sudo launchctl unload -w /System/Library/LaunchDaemons/org.apache.httpd.plist 2>/dev/null

# Tap Apache 
brew tap homebrew/dupes
brew tap homebrew/apache
brew install -v httpd22 --with-brewed-openssl

# Tap PHP
brew tap homebrew/php
brew install -v php55 --homebrew-apxs --with-apache

# Configure Apache with PHP
cat >> $(brew --prefix)/etc/apache2/2.2/httpd.conf <<EOF
# Send PHP extensions to mod_php
AddHandler php5-script .php
AddType text/html .php
DirectoryIndex index.php index.html
EOF

# Timezone
sed -i '-default' "s|^;\(date\.timezone[[:space:]]*=\).*|\1 \"$(sudo systemsetup -gettimezone|awk -F": " '{print $2}')\"|; s|^\(memory_limit[[:space:]]*=\).*|\1 256M|; s|^\(post_max_size[[:space:]]*=\).*|\1 200M|; s|^\(upload_max_filesize[[:space:]]*=\).*|\1 100M|; s|^\(default_socket_timeout[[:space:]]*=\).*|\1 600|; s|^\(max_execution_time[[:space:]]*=\).*|\1 300|; s|^\(max_input_time[[:space:]]*=\).*|\1 600|;" $(brew --prefix)/etc/php/5.5/php.ini

# PHP error log
USERHOME=$(dscl . -read /Users/`whoami` NFSHomeDirectory | awk -F": " '{print $2}') cat >> $(brew --prefix)/etc/php/5.5/php.ini <<EOF
; PHP Error log
error_log = ${USERHOME}/Sites/logs/php-error_log
EOF

# Pear and Pecl
touch $(brew --prefix php55)/lib/php/.lock && chmod 0644 $(brew --prefix php55)/lib/php/.lock

# OpCache extension will speed up your PHP environment dramatically
brew install -v php55-opcache
pecl install zendopcache-beta
 
sed -i '' "s|^\(zend_extension=\"\)\(opcache\.so\"\)|\1$(php -r 'print(ini_get("extension_dir")."/");')\2|" $(brew --prefix)/etc/php/5.3/php.ini
 
[ ! -d $(brew --prefix)/etc/php/5.3/conf.d ] && mkdir -pv $(brew --prefix)/etc/php/5.3/conf.d
 
echo "[opcache]" > $(brew --prefix)/etc/php/5.3/conf.d/ext-opcache.ini
 
grep -E '^zend_extension.*opcache\.so' $(brew --prefix)/etc/php/5.3/php.ini >> $(brew --prefix)/etc/php/5.3/conf.d/ext-opcache.ini
 
sed -i '' '/^zend_extension.*opcache\.so/d' $(brew --prefix)/etc/php/5.3/php.ini
 
# "php54" is not a typo here- I'm using a sample config file from
# another recipe for my config file in php53
grep -E '^[[:space:]]*opcache\.' \
  $(brew --prefix)/Library/Taps/homebrew/homebrew-php/Formula/php54-opcache.rb \
  | sed 's/^[[:space:]]*//g' >> $(brew --prefix)/etc/php/5.3/conf.d/ext-opcache.ini

sed -i '' "s|^\(opcache\.memory_consumption=\)[0-9]*|\1256|;" $(brew --prefix)/etc/php/5.5/conf.d/ext-opcache.ini

# Start Apache
ln -sfv $(brew --prefix httpd22)/homebrew.mxcl.httpd22.plist ~/Library/LaunchAgents
launchctl load -w ~/Library/LaunchAgents/homebrew.mxcl.httpd22.plist

# Install node
brew install node

# Update npm
sudo npm install npm -g

# Test node
vim server.js
paste: 
var http = require('http');
http.createServer(function (req, res) {
  res.writeHead(200, {'Content-Type': 'text/plain'});
  res.end('Hello World\n');
}).listen(8124, "127.0.0.1");
console.log('Server running at http://127.0.0.1:8124/');
save
node server.js
ctrl+c
rm server.js

# Test npm
npm -v

cd /var/www/html/SLR*/
sudo rm -rf .npm node_modules/
sudo npm cache clean

# Install grunt
sudo npm install -g grunt-cli
grunt --version

# Clone project
cd /var/www/html

# Dependencies
sudo npm install
sudo gem install compass
grunt

#mysql -u root -p
#YourPassword!
#create database slr_local;
#create user 'slr'@'localhost' identified by 'YourPassword!';
#grant all on slr_local.* to 'slr'@'localhost' identified by 'YourPassword!';
#mysql -u slr -p slr_local <slr_local.sql