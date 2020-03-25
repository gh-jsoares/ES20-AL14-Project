AMOUNT = 10000

count = 0
with open("StudentQuestionData.csv", "r") as f:
    print("Counting lines from file...")
    for i in f:
        count += 1
        
print("Initial line count: {}".format(count))
with open("StudentQuestionData.csv", "a+") as f:
    print("Appending lines to file...")
    for i in range(AMOUNT):
        f.write("Demo Question {}\n".format(i+count+1))

print("Final line count: {}".format(count+AMOUNT))
