import re
from selenium import webdriver
from selenium.common.exceptions import NoSuchElementException
from selenium.common.exceptions import WebDriverException
import time

browser = webdriver.Firefox() # Get local session of firefox
browser.get("http://46.16.232.131/freemium.html") # Load page
time.sleep(3) # Let the page load, will be added to the API

checkbox =  browser.find_element_by_class_name("zoom-checkbox")
checkbox=  checkbox.find_element_by_tag_name("input")

max = 1950
count = 0

r = open('./NCS-line.txt', 'r')
w = open('./NCS-result.txt', 'w')

for line in r:
    count += 1
    browser.execute_script("document.querySelector('.ncsnav-colortext').value = ''")
    ncsnav =  browser.find_element_by_class_name("ncsnav-colortext")
    ncsnav.send_keys(line)
    #checkbox.click()

    colorbox = browser.find_element_by_class_name("color-square")
    
    values =  re.findall(r"\d+", colorbox.value_of_css_property("background-color"))
    hex = '#%02x%02x%02x' % (int(values[0]), int(values[1]), int(values[2]))
    w.write(line.rstrip() + "," + hex + "\n");
    print count, " of ", max

w.close()
browser.close()



