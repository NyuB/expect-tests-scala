ifeq ($(OS), Windows_NT)
	MILLW=millw
else
	MILLW=./millw
endif

.PHONY: dev test

dev: fmt test

test:
	$(MILLW) expect.test

test-update:
	$(MILLW) expect.test testOnly *Suite* -Dnyub.expect=promote

test-clear:
	$(MILLW) expect.test testOnly *Suite* -Dnyub.expect=clear

# Clear then update all expect tests, then check there is no diff, meaning all the tests match exactly the output of ExpectTest
assert-up-to-date:
	$(MAKE) test-clear
	$(MAKE) test-update
	git diff --exit-code expect


fmt:
	scalafmt .

fmt-check:
	scalafmt --check .

clean:
	$(MILLW) clean
