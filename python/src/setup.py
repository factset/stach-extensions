import setuptools

REQUIRES = ["fds.protobuf.stach<2.0.0", "fds.protobuf.stach.v2<2.0.0", "pandas<2.0.0", "numpy<2.0.0"]

setuptools.setup(
    name="fds.protobuf.stach.extensions",
    version="1.1.0",
    author="Analytics API",
    author_email="analytics.api.support@factset.com",
    description="FactSet stach extensions",
    long_description = "Extension package to help convert stach format data to simpler data structures",
    url="https://github.com/factset/stach-extensions",
    packages=setuptools.find_packages(exclude=["test", "tests"]),
    install_requires=REQUIRES,
    include_package_data=True,
    license="Apache License 2.0",
    python_requires=">=3.6",
)
