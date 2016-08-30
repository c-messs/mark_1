#!/usr/bin/env ruby
#require 'nokogiri'
require 'rexml/document'
ARGV.each { |filename|
  print "Reading #{filename}"
  begin
    doc = REXML::Document.new(File.open(filename));
    formatter = REXML::Formatters::Pretty.new
    formatter.compact = true
    f=File.open(filename,"w")
    formatter.write(doc, f)
    f.close
    puts " complete"
  rescue Exception=>e
    puts " failed( #{e.message} )" 
  end
}

