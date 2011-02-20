

alias x='$(find . -name $TARGET) --logtostderr'
alias c='rm $(find . -name $TARGET); make $TARGET;'
alias r='rm $(find . -name $TARGET); make $TARGET; x'

