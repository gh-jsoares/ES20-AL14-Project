from os import path

AMOUNT = 10000
FILE_NAME = "StudentQuestionData.csv"

count = 0
if path.exists(FILE_NAME):
    with open(FILE_NAME, "r") as f:
        print("Counting lines from file...")
        for i in f:
            count += 1


print("Initial line count: {}".format(count))
with open(FILE_NAME, "a+") as f:
    print("Appending lines to file...")
    for i in range(AMOUNT):
        id = i+count+1
        f.write("{},Demo Question {}\n".format(id, id))

print("Final line count: {}".format(count+AMOUNT))
