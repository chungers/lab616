

alias x='$(find . -name $TARGET) --logtostderr'
alias r='rm $(find . -name $TARGET); make $TARGET; x'

