
  Pod::Spec.new do |s|
    s.name = 'Yandex.Locator'
    s.version = '0.0.1'
    s.summary = 'This is plugin for get geolocation via GSM and Wifi'
    s.license = 'MIT'
    s.homepage = 'git@github.com:melnikovpa/capacitor.yandex.geolocation.git'
    s.author = 'Pavel Melnikov'
    s.source = { :git => 'git@github.com:melnikovpa/capacitor.yandex.geolocation.git', :tag => s.version.to_s }
    s.source_files = 'ios/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}'
    s.ios.deployment_target  = '11.0'
    s.dependency 'Capacitor'
  end