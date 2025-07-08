FROM python:3.9.18

RUN pip install "ray[default]==2.44.1"
RUN pip install "numpy>=1.24,<2"

CMD ["bash", "-c", "ray start --head --num-cpus 1 --dashboard-host 0.0.0.0 --include-dashboard true --block"]