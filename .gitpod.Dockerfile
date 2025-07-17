FROM gitpod/workspace-full

# Install required packages
RUN sudo apt-get update \
 && sudo apt-get install -y openjdk-17-jdk \
 && sudo update-java-alternatives -s java-1.17.0-openjdk-amd64

# Set environment variables
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV ANDROID_SDK_ROOT=/workspace/android-sdk
ENV PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools

# Install additional tools needed for Android development
RUN sudo apt-get install -y \
    libglu1-mesa \
    libpulse0 \
    libxcursor1 \
    libxdamage1 \
    libxi6 \
    libxinerama1 \
    libxrandr2 \
    libxtst6 \
    zip \
    unzip
