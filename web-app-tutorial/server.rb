require 'bundler/setup'
require 'sinatra'
require 'oauth2'
require 'json'
require "net/http"
require "uri"

# Our client ID and secret, used to get the access token
# CLIENT_ID = ENV['ST_CLIENT_ID']
# CLIENT_SECRET = ENV['ST_CLIENT_SECRET']


# We'll store the access token in the session
use Rack::Session::Pool, :cookie_only => false


# This is the URI that will be called with our access
# code after we authenticate with our SmartThings account
redirect_uri = 'http://localhost:4567/oauth/callback'

# This is the URI we will use to get the endpoints once we've received our token
endpoints_uri = 'https://graph.api.smartthings.com/api/smartapps/endpoints'

options = {
  site: 'https://graph.api.smartthings.com',
  authorize_url: '/oauth/authorize',
  token_url: '/oauth/token'
}

# use the OAuth2 module to handle OAuth flow
client = OAuth2::Client.new(CLIENT_ID, CLIENT_SECRET, options)

# helper method to know if we have an access token
def authenticated?
  session[:access_token]
end

# handle requests to the application root
get '/' do
  %(<a href="/authorize">Connect with SmartThings</a>)
end

# handle requests to /authorize URL
get '/authorize' do
    # Use the OAuth2 module to get the authorize URL.
  # After we authenticate with SmartThings, we will be redirected to the
  # redirect_uri, including our access code used to get the token
  url = client.auth_code.authorize_url(redirect_uri: redirect_uri, scope: 'app')
  redirect url
end

# hanlde requests to /oauth/callback URL. We
# will tell SmartThings to call this URL with our
# authorization code once we've authenticated.
get '/oauth/callback' do
  # The callback is called with a "code" URL parameter
  # This is the code we can use to get our access token
  code = params[:code]
    
  # # Use the code to get the token.
  response = client.auth_code.get_token(code, redirect_uri: redirect_uri, scope: 'app')

  # now that we have the access token, we will store it in the session
  session[:access_token] = response.token

  # debug - inspect the running console for the
  # expires in (seconds from now), and the expires at (in epoch time)
  puts 'TOKEN EXPIRES IN ' + response.expires_in.to_s
  puts 'TOKEN EXPIRES AT ' + response.expires_at.to_s
  return 'TOKEN IS ' + response.token
  
  # redirect '/getswitch'
end

get '/ahoy' do
    theWord='ahoy'
    return theWord
end

# handle requests to the /getSwitch URL. This is where
# we will make requests to get information about the configured
# switch.
get '/getswitch' do
    'Not Implemented!'
end